import {useRepository} from "../../../../../hooks/useRepository";
import {ReactNode, useEffect, useState} from "react";
import {components} from "../../../../../service/apiSchema";
import {container} from "tsyringe";
import {ImportExportService} from "../../../../../service/ImportExportService";
import {T} from "@tolgee/react";
import {startLoading, stopLoading} from "../../../../../hooks/loading";
import {ImportActions} from "../../../../../store/repository/ImportActions";

const service = container.resolve(ImportExportService)
const actions = container.resolve(ImportActions)

export const useImportRequest = () => {
    const repository = useRepository()
    const [result, setResult] = useState(undefined as components["schemas"]["PagedModelImportLanguageModel"] | undefined)
    const [progressMessage, setProgressMessage] = useState(undefined as ReactNode | undefined)
    const cancelLoadable = actions.useSelector(s => s.loadables.cancelImport)
    const deleteLanguageLoadable = actions.useSelector(s => s.loadables.deleteLanguage)

    const onMessage = (type: string, ...params) => {
        setProgressMessage(<T>{type.toLocaleLowerCase()}</T>)
    }

    const onResult = (result) => {
        setResult(result)
    }

    const onNewFiles = async (files: File[]) => {
        let buffer = ""

        const onData = (isDone: boolean) => {
            const delimiter = ";;;"
            let delimiterIndex = buffer.indexOf(delimiter)
            while (delimiterIndex > -1 || isDone) {
                if (delimiterIndex === -1 && isDone) {
                    delimiterIndex = buffer.length
                }
                const jsonString = buffer.substring(0, delimiterIndex)
                buffer = buffer.substring(delimiterIndex + delimiter.length, buffer.length)
                if (jsonString.length > 0) {
                    const data = JSON.parse(jsonString)
                    if (!isDone) {
                        onMessage(data.type, ...data.params)
                    } else {
                        onResult(data)
                        break
                    }
                }
                delimiterIndex = buffer.indexOf(delimiter)
            }
        }

        try {
            const response = await service.addFiles(repository.id, files)
            const reader = response.body!.getReader()
            let done = false
            while (!done) {
                const read = await reader.read()
                buffer += new TextDecoder().decode(read.value)
                done = read.done
                onData(done)
            }
        } catch (e) {
            console.error(e)
        }
    }

    const loadData = () => {
        startLoading()
        service.loadData(repository.id, {page: 0, size: 100}).then(r => {
            setResult(r)
        }).catch(e => {
            setResult(undefined)
            console.error(e)
        }).finally(() => {
            stopLoading()
        })
    }

    useEffect(() => {
        if (deleteLanguageLoadable.loaded || !deleteLanguageLoadable.touched) {
            startLoading()
            loadData()
        }
    }, [deleteLanguageLoadable.loading])


    useEffect(() => {
        if (cancelLoadable.loaded) {
            setResult(undefined)
        }
    }, [cancelLoadable.touched, cancelLoadable.loading])

    return {
        onNewFiles, result, progressMessage
    }
}
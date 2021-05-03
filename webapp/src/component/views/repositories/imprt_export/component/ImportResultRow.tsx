import {components} from "../../../../../service/apiSchema";
import React from "react";
import {Button, IconButton, TableCell, TableRow} from "@material-ui/core";
import DeleteIcon from "@material-ui/icons/Delete";
import {ImportActions} from "../../../../../store/repository/ImportActions";
import {container} from "tsyringe";
import {useRepository} from "../../../../../hooks/useRepository";
import {confirmation} from "../../../../../hooks/confirmation";

const actions = container.resolve(ImportActions)
export const ImportResultRow = (props: {
    row: components["schemas"]["ImportLanguageModel"]
    onResolveConflicts: (row: components["schemas"]["ImportLanguageModel"]) => void
}) => {
    const repository = useRepository()

    const deleteLanguage = () => {
        confirmation({
            onConfirm: () => actions.loadableActions.deleteLanguage.dispatch({path: {languageId: props.row.id, repositoryId: repository.id}})
        })
    };

    return (
        <React.Fragment>
            <TableRow>
                <TableCell scope="row">
                    {props.row.existingLanguageName}
                </TableCell>
                <TableCell scope="row">
                    {props.row.importFileName}
                </TableCell>
                <TableCell scope="row">
                    <Button size="small">{props.row.totalCount}</Button>
                </TableCell>
                <TableCell scope="row">
                    <Button onClick={() => props.onResolveConflicts(props.row)} size="small">{props.row.conflictCount}</Button>
                </TableCell>
                <TableCell scope="row" align={"right"}>
                    <IconButton size="small" style={{padding: 0}}>
                        <DeleteIcon onClick={deleteLanguage}/>
                    </IconButton>
                </TableCell>
            </TableRow>
        </React.Fragment>
    );
}
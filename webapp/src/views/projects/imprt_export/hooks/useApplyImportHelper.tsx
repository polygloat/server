import { useEffect, useState } from 'react';
import { startLoading, stopLoading } from 'tg.hooks/loading';
import { useProject } from 'tg.hooks/useProject';
import { ImportActions } from 'tg.store/project/ImportActions';
import { container } from 'tsyringe';
import { useImportDataHelper } from './useImportDataHelper';

const actions = container.resolve(ImportActions);
export const useApplyImportHelper = (
  dataHelper: ReturnType<typeof useImportDataHelper>
) => {
  const [conflictNotResolvedDialogOpen, setConflictNotResolvedDialogOpen] =
    useState(false);

  const importApplyLoadable = actions.useSelector(
    (s) => s.loadables.applyImport
  );
  const project = useProject();
  const error = importApplyLoadable.error;

  useEffect(() => {
    if (importApplyLoadable.loading) {
      startLoading();
      return;
    }
    stopLoading();
  }, [importApplyLoadable.loading]);

  const onApplyImport = () => {
    const unResolvedCount = dataHelper.result?._embedded?.languages?.reduce(
      (acc, curr) => acc + curr.conflictCount - curr.resolvedCount,
      0
    );
    if (unResolvedCount === 0) {
      actions.loadableActions.applyImport.dispatch({
        path: {
          projectId: project.id,
        },
        query: {},
      });
      return;
    }
    dataHelper.loadData();
    setConflictNotResolvedDialogOpen(true);
  };

  useEffect(() => {
    const error = importApplyLoadable.error;
    if (error?.code == 'conflict_is_not_resolved') {
      setConflictNotResolvedDialogOpen(true);
      return;
    }
  }, [importApplyLoadable.error]);

  useEffect(() => {
    startLoading();
    if (importApplyLoadable.loaded) {
      dataHelper.loadData();
    }
  }, [importApplyLoadable.loading]);

  const onDialogClose = () => {
    setConflictNotResolvedDialogOpen(false);
  };

  return { onDialogClose, onApplyImport, conflictNotResolvedDialogOpen, error };
};

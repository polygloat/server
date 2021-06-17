import { FunctionComponent, useEffect } from 'react';
import { container } from 'tsyringe';
import { useSelector } from 'react-redux';
import { AppState } from '../store';
import { GlobalError } from '../error/GlobalError';
import { LanguageActions } from '../store/languages/LanguageActions';
import { useProject } from './useProject';
import { FullPageLoading } from '../component/common/FullPageLoading';
import { ProjectPreferencesService } from '../service/ProjectPreferencesService';
import { TranslationActions } from '../store/project/TranslationActions';

const languageActions = container.resolve(LanguageActions);
const translationActions = container.resolve(TranslationActions);
const selectedLanguagesService = container.resolve(ProjectPreferencesService);

export const ProjectLanguagesProvider: FunctionComponent = (props) => {
  const projectDTO = useProject();

  const languagesLoadable = useSelector(
    (state: AppState) => state.languages.loadables.globalList
  );

  const isLoading = languagesLoadable.loading;
  const init =
    !languagesLoadable.data && !languagesLoadable.error && !isLoading;
  const idChanged =
    languagesLoadable.dispatchParams &&
    languagesLoadable.dispatchParams[0].path.projectId !== projectDTO.id;

  useEffect(() => {
    if (init || idChanged) {
      languageActions.loadableActions.globalList.dispatch({
        path: { projectId: projectDTO.id },
        query: {
          pageable: {
            page: 0,
            size: 1000,
            sort: ['tag'],
          },
        },
      });
      const selectedLanguages = selectedLanguagesService.getForProject(
        projectDTO.id
      );
      translationActions.select.dispatch(Array.from(selectedLanguages));
    }
  }, [init]);

  if (init || idChanged || languagesLoadable.loading) {
    return <FullPageLoading />;
  }

  if (languagesLoadable.data) {
    return <>{props.children}</>;
  }

  throw new GlobalError(
    'Unexpected error occurred',
    languagesLoadable.error?.code || 'Loadable error'
  );
};

import { default as React, FunctionComponent } from 'react';
import { useSelector } from 'react-redux';
import { Redirect, Route } from 'react-router-dom';
import { LINKS } from 'tg.constants/links';
import { SecurityService } from 'tg.service/SecurityService';
import { AppState } from 'tg.store/index';
import { container } from 'tsyringe';

interface PrivateRouteProps {}

export const PrivateRoute: FunctionComponent<
  PrivateRouteProps & React.ComponentProps<typeof Route>
> = (props) => {
  const allowPrivate = useSelector(
    (state: AppState) => state.global.security.allowPrivate
  );
  const ss = container.resolve(SecurityService);
  const afterLoginLink = ss.getAfterLoginLink();

  if (allowPrivate && afterLoginLink) {
    ss.removeAfterLoginLink();
    return <Redirect to={afterLoginLink} />;
  }

  if (allowPrivate) {
    return <Route {...props} />;
  }

  return (
    <Route
      render={({ location }) => (
        <Redirect
          to={{
            pathname: LINKS.LOGIN.build(),
            state: { from: location },
          }}
        />
      )}
    />
  );
};

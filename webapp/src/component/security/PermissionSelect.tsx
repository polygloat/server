import { Select } from '../common/form/fields/Select';
import { projectPermissionTypes } from '../../constants/projectPermissionTypes';
import { ComponentProps, default as React, FunctionComponent } from 'react';
import { MenuItem } from '@material-ui/core';
import { translatedPermissionType } from '../../fixtures/translatePermissionFile';

export const PermissionSelect: FunctionComponent<
  ComponentProps<typeof Select>
> = (props) => {
  return (
    <Select {...props} renderValue={(v) => translatedPermissionType(v)}>
      {Object.keys(projectPermissionTypes).map((k) => (
        <MenuItem data-cy="permission-select-item" key={k} value={k}>
          {translatedPermissionType(k)}
        </MenuItem>
      ))}
    </Select>
  );
};

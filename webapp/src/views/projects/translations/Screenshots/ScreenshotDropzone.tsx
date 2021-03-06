import React, { FunctionComponent, useState } from 'react';
import { Box, createStyles, makeStyles, Theme } from '@material-ui/core';
import { green, red } from '@material-ui/core/colors';
import BackupTwoToneIcon from '@material-ui/icons/BackupTwoTone';
import HighlightOffTwoToneIcon from '@material-ui/icons/HighlightOffTwoTone';
import clsx from 'clsx';

import { FileUploadFixtures } from 'tg.fixtures/FileUploadFixtures';
import { useProjectPermissions } from 'tg.hooks/useProjectPermissions';
import { ProjectPermissionType } from 'tg.service/response.types';

import { MAX_FILE_COUNT } from './ScreenshotGallery';

export interface ScreenshotDropzoneProps {
  validateAndUpload: (files: File[]) => void;
}

const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    dropZoneValidation: {
      pointerEvents: 'none',
      opacity: 0,
      transition: 'opacity 0.2s',
    },
    valid: {
      backdropFilter: 'blur(5px)',
      border: `10px solid ${green[200]}`,
      backgroundColor: theme.palette.grey[200],
      opacity: 0.9,
    },
    invalid: {
      border: `10px solid ${red[200]}`,
      opacity: 0.9,
      backgroundColor: theme.palette.grey[200],
      backdropFilter: 'blur(5px)',
    },
  })
);

export const ScreenshotDropzone: FunctionComponent<ScreenshotDropzoneProps> = ({
  validateAndUpload,
  ...props
}) => {
  const [dragOver, setDragOver] = useState(null as null | 'valid' | 'invalid');
  const [dragEnterTarget, setDragEnterTarget] = useState(
    null as EventTarget | null
  );
  const classes = useStyles({});
  const projectPermissions = useProjectPermissions();

  const onDragEnter = (e: React.DragEvent) => {
    e.stopPropagation();
    e.preventDefault();
    setDragEnterTarget(e.target);
    if (e.dataTransfer.items) {
      const files = FileUploadFixtures.dataTransferItemsToArray(
        e.dataTransfer.items
      );
      if (files.length > MAX_FILE_COUNT) {
        setDragOver('invalid');
        return;
      }
      setDragOver('valid');
    }
  };

  const onDragLeave = (e: React.DragEvent) => {
    e.stopPropagation();
    e.preventDefault();
    if (e.target === dragEnterTarget) {
      setDragOver(null);
    }
  };

  const onDrop = async (e: React.DragEvent) => {
    e.stopPropagation();
    e.preventDefault();
    if (e.dataTransfer.items) {
      const files = FileUploadFixtures.dataTransferItemsToArray(
        e.dataTransfer.items
      );
      validateAndUpload(files);
    }
    setDragOver(null);
  };

  let dropZoneAllowedProps = {};
  if (projectPermissions.satisfiesPermission(ProjectPermissionType.TRANSLATE)) {
    dropZoneAllowedProps = { onDrop, onDragEnter, onDragLeave };
  }

  return (
    <>
      <Box
        position="relative"
        display="flex"
        {...dropZoneAllowedProps}
        overflow="visible"
        data-cy="dropzone"
      >
        <Box
          zIndex={2}
          position="absolute"
          width="100%"
          height="100%"
          className={clsx({
            [classes.dropZoneValidation]: true,
            [classes.valid]: dragOver === 'valid',
            [classes.invalid]: dragOver === 'invalid',
          })}
          display="flex"
          alignItems="center"
          justifyContent="center"
        >
          {dragOver === 'valid' && (
            <BackupTwoToneIcon style={{ fontSize: 150 }} />
          )}
          {dragOver === 'invalid' && (
            <HighlightOffTwoToneIcon style={{ fontSize: 150 }} />
          )}
        </Box>
        {props.children}
      </Box>
    </>
  );
};

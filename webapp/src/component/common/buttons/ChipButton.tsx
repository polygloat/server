import * as React from 'react';
import {FunctionComponent, ReactNode} from 'react';
import Box from "@material-ui/core/Box";
import {Button, makeStyles} from "@material-ui/core";
import clsx from "clsx";

const useStyles = makeStyles(theme => ({
    root: {
        border: `1px solid ${theme.palette.grey["200"]}`,
        borderRadius: 50,
        padding: `${theme.spacing(0.125)}px ${theme.spacing(1.5)}px`,
        backgroundColor: theme.palette.common.white,
        cursor: "pointer",
        minWidth: "0",
    },
    icon: {
        display: "inline-flex",
        alignItems: "center",
        "& svg": {
            fontSize: 16,
        }
    },
    beforeIcon: {
        marginRight: theme.spacing(0.5),
    }
}))

export const ChipButton: FunctionComponent<{
    beforeIcon?: ReactNode,
    onClick: () => void
}> = (props) => {

    const classes = useStyles()

    return (
        <Button onClick={props.onClick} className={classes.root}>
            {props.beforeIcon && <Box display="inline-flex" className={clsx(classes.beforeIcon, classes.icon)}>
                {props.beforeIcon}
            </Box>}
            {props.children}
        </Button>
    )
}

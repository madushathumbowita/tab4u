import { Typography } from '@mui/material';
import React from 'react';
import {ReactComponent as Logo} from './Tab4u.svg';
import image from './../static/images/logo 52.png';

export default function ApplicationName() {
    return (
        <div className="flex xl:h-full h-1/6 m-auto justify-center items-center xl:ml-16 mt-10 xl:m-0">
            {/* <Logo  /> */}
            <img src={image} />
        </div>
    );
}


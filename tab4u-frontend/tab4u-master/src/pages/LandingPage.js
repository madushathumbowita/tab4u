import React from 'react';
import ApplicationName from '../components/ApplicationName';
import AuthenticationContainer from '../components/AuthenticationContainer';

export default function LandingPage() {
    return (
        <div className='h-screen w-screen bg-gradient-to-r from-blue-500 via-purple-500 to-purple-500'>
            <div className='flex flex-col xl:flex-row h-full w-full'>
                <ApplicationName />
                <AuthenticationContainer />
            </div>
        </div>
    );
}

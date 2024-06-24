import React, { useEffect, useState } from 'react';

export default function Popup({ error, setErrorMessage }) {
    const [visible, setVisible] = useState(true);

    useEffect(() => {
        if (error) {
            setVisible(true);
            const timer = setTimeout(() => {
                setErrorMessage("");
                setVisible(false);
            }, 5000);

            return () => {
                clearTimeout(timer)
            };
        }
    }, [error]);

    if (!visible || !error) return null;

    return (
        <div className="fixed top-0 left-0 right-0 bg-red-500 text-white text-center p-4">
            {error}
        </div>
    );
}

import React, { useEffect, useState, forwardRef } from 'react';

const GuitarTab = forwardRef(({ tablature }, ref) => {
    const strings = tablature ? tablature.length : 0;
    const [windowWidth, setWindowWidth] = useState(window.innerWidth);

    // Update windowWidth state on resize
    useEffect(() => {
        const handleResize = () => setWindowWidth(window.innerWidth);
        window.addEventListener('resize', handleResize);
        return () => window.removeEventListener('resize', handleResize);
    }, []);

    // Function to render each string and its frets
    const renderString = (stringData, stringIndex) => {
        // Calculate x-position for each fret
        const getXLength = (fretIndex) => {
            const numberOfFrets = stringData.length;
            const distanceBetweenFrets = (0.6 * windowWidth) / numberOfFrets; // Adjust width factor if needed
            if (fretIndex === 0) return 15; // Offset for first fret
            return distanceBetweenFrets * fretIndex;
        };

        return (
            <g key={stringIndex}>
                {stringIndex < 6 && (
                    <>
                        <text x="10" y={15 + 20 * stringIndex} fontSize="12" >
                            {stringIndex == 0 &&  `String ${stringIndex + 1}  (e)` }
                            {stringIndex == 1 &&  `String ${stringIndex + 1}  (B)` }
                            {stringIndex == 2 &&  `String ${stringIndex + 1}  (G)` }
                            {stringIndex == 3 &&  `String ${stringIndex + 1}  (D)` }
                            {stringIndex == 4 &&  `String ${stringIndex + 1}  (A)` }
                            {stringIndex == 5 &&  `String ${stringIndex + 1}  (E)` }
                        </text>
                        <line
                            x1="80"
                            y1={10 + 20 * stringIndex}
                            x2={0.6 * windowWidth + 80}
                            y2={10 + 20 * stringIndex}
                            stroke="black"
                            strokeDasharray="5,5"
                        />
                    </>
                )}
                {stringIndex === 6 && (
                    <text x="10" y={15 + 20 * stringIndex} fontSize="12">
                        Finger
                    </text>
                )}
                {stringData.map((fretData, fretIndex) => (
                    <text key={fretIndex} x={getXLength(fretIndex) + 80} y={15 + 20 * stringIndex} fontSize="12" className='font-bold'>
                        {fretData}
                    </text>
                ))}
            </g>
        );
    };

    return (
        <svg height={20 * strings + 20} width={"100%"} ref={ref}>
            {tablature && tablature.map(renderString) }
        </svg>
    );
});

const Output = forwardRef(({ data }, ref) => {
    return <GuitarTab tablature={data} ref={ref} />;
});

export default Output;
import axios from 'axios';

const API_ENDPOINT = "http://localhost:8080";

export async function generateTablature(audioFile) {
    const PREDICTION_ROUTE = "/api/generate";
    try {
        const formData = new FormData();
        formData.append('audioFile', audioFile);

        const response = await axios.post(API_ENDPOINT + PREDICTION_ROUTE, formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
        return response.data;
    }
    catch (error) {
        console.log(error);
        return null
    }
};
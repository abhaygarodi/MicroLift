import axios from 'axios';

const MEDIA_URL = 'http://localhost:8080/api/media';

const mediaService = {
    uploadFile: async (file) => {
        const formData = new FormData();
        formData.append('file', file);

        const response = await axios.post(`${MEDIA_URL}/upload`, formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
        return response.data; // Expected { url: "..." }
    }
};

export default mediaService;

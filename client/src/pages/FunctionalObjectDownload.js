import { useState } from 'react';
import axios from 'axios';
import DoughnutChart from '../Components/CustomDoughnutChart';
import ProgressComponent from '../Components/CustomProgressBar';
import Navbar from '../Components/Navbar';
import { ProgressBar } from 'react-bootstrap';
import Box from '@mui/material/Box';
import TextField from '@mui/material/TextField';

function FODownload() {
    const [name, setName] = useState([]);
    const [file, setFile] = useState(null);
    const [res, setRes] = useState([]);
    const [error, setError] = useState([]);
    const [success, setSuccess] = useState([]);
    const [uploadProgress, setUploadProgress] = useState(0);
    const [uploadStarted, setUploadStarted] = useState(false);
    const [uploadComplete, setUploadComplete] = useState(false);
    const [responseReceived, setResponseReceived] = useState(false);


    const baseURL = 'http://localhost:8080';

    const DownloadFile = (response) => {
        const contentDispositionHeader = response.headers['content-disposition'];
        const fileName = contentDispositionHeader
            ? contentDispositionHeader.split('filename=')[1].trim()
            : 'downloadedFile.xls';
    
        const blob = new Blob([response.data], { type: 'application/vnd.ms-excel' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', fileName);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    };    

    const donutChart ={
        chart1: {
            label: "Summary",
            series: [success,error],
            colors: ["#FFD668", "#00e396"],
            dataLabels: ["Success", "Error"]
        }
    }

    const handleArrayUpload = () => {
        // insert the input value into the array
        const objArray = document.getElementById('standard-basic').value.split(',');
        console.log(objArray);

        // send the array to the server with access token
        const params = new URLSearchParams();
        objArray.forEach(id => params.append('objArray', id));
        params.append('accessToken',localStorage.getItem('accessToken'));

        axios.get(`${baseURL}/api/getInfo?${params.toString()}`, {
            responseType: 'blob'
        })
        .then(response => {
           console.log(response);
           setFile(response);
           setResponseReceived(true);
        })
        .catch(error => {
            console.log(error);
        });
    }

    const handleFileDownload = () => {
        DownloadFile(file);
    }
  
    return (
        <div className='home-container'>
        <Navbar />
        <center>
        <h1 className='text-4xl text-blue-400 p-4'>Download functional object information</h1>
        {/* textfield */}
        
        <br />
        <p>Enter the object IDs as comma separated set of values</p>
        <TextField id="standard-basic" label="Object ID" variant="filled" className='mb-5' />
        <br />
        <button className="bg-sky-400 p-2 rounded-lg" onClick={handleArrayUpload}>
           Send Object ID
        </button>

        <br /> 
        <br />

        {uploadStarted && (
            <ProgressBar now={uploadProgress} label={`${uploadProgress}%`} style={{ width: '400px' }} />
        )}

        {uploadComplete && (
            <center>
                <h5>File uploaded successfully!</h5>
            </center>
        )}

        <hr />
        <br />

        {responseReceived>0 && (
            <>
                <p className="text-green-500">Download the functional object Details from here.</p>
                <button className="bg-sky-400 p-2 rounded-lg" onClick={handleFileDownload}>
                    Download
                </button>
            </>
        )}

        </center>

        {res === "Success" && (
           <DoughnutChart data={donutChart.chart1}/>
        )}        
        </div>
    );
}

export default FODownload;

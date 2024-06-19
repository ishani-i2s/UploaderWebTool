import { useState } from 'react';
import axios from 'axios';
import DoughnutChart from '../Components/CustomDoughnutChart';
import ProgressComponent from '../Components/CustomProgressBar';
import Navbar from '../Components/Navbar';
import { ProgressBar } from 'react-bootstrap';

function TaskDetailsUpdate() {
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
            : 'downloadedFile.xlsx';
    
        const blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
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

    const handleFileUpload = () => {
        setUploadProgress(40);
        setUploadStarted(true);
        const formData = new FormData();
        formData.append('file', file);
        formData.append('accessToken', localStorage.getItem('accessToken'));
        // formData.append('Name', name);
        axios.post(`${baseURL}/api/TaskDetailsUpdate`, formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            },
        })
        .then(response => {
            console.log(response);
            setRes("Success");
            // setError(response.data.errorCount);
            // setSuccess(response.data.successCount);
            setUploadProgress(100);
            // console.log(response.data.errorCount);
            // console.log(response.data.successCount);
            setResponseReceived(true);
        })
        .catch(err => {
            console.log(err);
            setRes(err.message);
            setUploadComplete(false);
        })
    }
      
    const handleFileDownload = () => {
        axios.get(`${baseURL}/api/excelDownload`, { responseType: 'blob' })
        .then(response => {
           DownloadFile(response);
        })
        .catch(err => {
            console.log(err);
            setRes(err.message);
        })
    }

    const setDetails = (e) => {
        const file = e.target.files[0];
        setName(file.name);
        setFile(file);
    }
  
    return (
        <div className='home-container'>
        <Navbar />
        <center>
        <h1 className='text-4xl text-blue-400 p-4'>Update Task Details</h1>
            <input type="file" onChange={setDetails} />
        <br />

        <button className="bg-sky-400 p-2 rounded-lg" onClick={handleFileUpload}>
            Upload File
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

        {error>0 && (
            <>
                <p className="text-red-500">There are few errors in the file. Please download the error file and correct the errors.</p>
                <button className="bg-sky-400 p-2 rounded-lg" onClick={handleFileDownload}>
                    Download Error File
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

export default TaskDetailsUpdate;

import * as React from 'react';
import Box from '@mui/material/Box';
import TextField from '@mui/material/TextField';
import MenuItem from '@mui/material/MenuItem';
import Navbar from '../Components/Navbar';
import Stack from '@mui/material/Stack';
import Button from '@mui/material/Button';
import { useEffect } from 'react';
import axios from 'axios';
import { useState } from 'react';
import { set } from 'date-fns';

const baseURL = 'http://localhost:8080';

export default function RouteChanges() {
    const [sites, setSites] = useState([]);
    const [selectedSite, setSelectedSite] = useState('');
    const [selectedStatus, setSelectedStatus] = useState('');
    const [responseReceived, setResponseReceived] = useState(false);
    const [selectedPlannedStartDate, setSelectedPlannedStartDate] = useState('');
    const [file, setFile] = useState([]);

    const status = [
        { value: 'New', label: 'New' },
        { value: 'Under Preparation', label: 'Under Preparation' },
        { value: 'Prepared', label: 'Prepared' },
        { value: 'Released', label: 'Released' },
        { value: 'Work Started', label: 'Work Started' },
        { value: 'Work Done', label: 'Work Done' },
        { value: 'Reported', label: 'Reported' },
        { value: 'Finished', label: 'Finished' },
        { value: 'Cancelled', label: 'Cancelled' }
    ];

    useEffect(() => {
        const accessToken = localStorage.getItem('accessToken');

        axios.get(`${baseURL}/api/getSites`, {
            params: {
                accessToken: accessToken
            },
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .then((response) => {
            if (response.data.length === 0) {
                console.log("No data found");
            } else {
                // Assuming response.data is an array of strings (site names)
                let options = response.data.map((site) => ({
                    value: site,
                    label: site
                }));
                setSites(options);
            }
        })
        .catch((err) => {
            console.log(err);
        });
    }, []);

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

    const sendRequest = (event) => {
        event.preventDefault();
        const accessToken = localStorage.getItem('accessToken');
        const data = {
            accessToken: accessToken,
            site: extractSite(selectedSite),
            status: selectedStatus,
            plannedStart: selectedPlannedStartDate
        };
        console.log(data);
        
        axios.get(`${baseURL}/api/getRouteChanges`, {
            params: data,
            headers: {
                'Content-Type': 'application/json'
            },
            responseType: 'blob'
        })
        .then((response) => {
            console.log(response);
            setFile(response);
            setResponseReceived(true);
        })
        .catch((err) => {
            console.log(err);
        });
    }

    const extractSite = (site) => {
        console.log(site);
        return site.split('-')[0];
    }

    // Log the state to debug
    useEffect(() => {
        console.log("Selected Site:", selectedSite);
        console.log("Selected Status:", selectedStatus);
        console.log("Selected Planned Start Date:", selectedPlannedStartDate);
    }, [selectedSite, selectedStatus, selectedPlannedStartDate]);

    const handleFileDownload = () => {
        DownloadFile(file);
    }

    return (
        <>
            <Navbar />
            <center>
                <h1 className='text-4xl text-blue-400 p-4'>Route Changes</h1>
                <br />
                <Box
                    component="form"
                    sx={{
                        '& .MuiTextField-root': { m: 1, width: '50ch' },
                    }}
                    noValidate
                    autoComplete="off"
                >
                    <div>
                        <TextField
                            id="filled-select-site"
                            select
                            SelectProps={{
                                native: true,
                            }}
                            helperText="Select the site for which the route changes are to be made"
                            value={selectedSite}
                            onChange={(e) => setSelectedSite(e.target.value)}
                        >
                            <option value="">Select Site</option>
                            {sites.map((option) => (
                                <option key={option.value} value={option.value}>
                                    {option.label}
                                </option>
                            ))}
                        </TextField>
                    </div>
                    <div>
                        <TextField
                            id="filled-select-status"
                            select
                            SelectProps={{
                                native: true,
                            }}
                            helperText="Select the status for which the route changes are to be made"
                            value={selectedStatus}
                            onChange={(e) => setSelectedStatus(e.target.value)}
                        >
                            <option value="">Select Status</option>
                            {status.map((option) => (
                                <option key={option.value} value={option.value}>
                                    {option.label}
                                </option>
                            ))}
                        </TextField>
                    </div>
                    <div>
                        <TextField
                            id="date"
                            label="Planned Start Date"
                            type="date"
                            sx={{ width: 220 }}
                            InputLabelProps={{
                                shrink: true,
                            }}
                            value={selectedPlannedStartDate}
                            onChange={(e) => setSelectedPlannedStartDate(e.target.value)}
                        />
                    </div>
                    <Button variant="contained" onClick={sendRequest}>Submit</Button>
                </Box>

                {responseReceived>0 && (
                    <>
                        <p className="text-green-500">Download the Task Details from here</p>
                        <button className="bg-sky-400 p-2 rounded-lg" onClick={handleFileDownload}>
                            Download
                        </button>
                    </>
                )}
            </center>
        </>
    );
}

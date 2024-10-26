import * as React from 'react';
import Typography from '@mui/material/Typography';
import Link from '@mui/material/Link';


export default function Copyright(props) {
  

    return (
      <Typography variant="body2" color="text.primary" align="center" {...props}>
        <Link color="inherit" sx={{textDecoration:"none"}}>
          Made with Love by Lulu, Kris, Izzy & Vee
        </Link>{' '}
        {new Date().getFullYear()}
        {'.'}
      </Typography>
    );
  }
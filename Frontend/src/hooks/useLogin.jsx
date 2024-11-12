// import { useState } from 'react';
// import { useAuthContext } from './useAuthContext.jsx';

// export const useLogin = () =>{
//     const [error, setError] = useState(null);
//     const [isLoading, setIsLoading] = useState(null);
//     const { dispatch } = useAuthContext();

//     const login = async (email, password) =>{
//         setIsLoading(true)
//         setError(null)
        

//         const url = "http://localhost:8080/user/login/";
//         const response = await fetch (url, {
//             method: 'POST',
//             headers:{'Content-Type':'application/json'},
//             body: JSON.stringify({email, password})
//         })
//         const json = await response.json()

//         if(!response.ok){
//             setIsLoading(false)
//             setError(json.error)
//             return false;
//         }
//         if(response.ok){
//             // save the user to local browser storage
//             localStorage.setItem('user',JSON.stringify(json))

//             // Update the auth context
//             dispatch({type:'LOGIN', payload: json});
//             setIsLoading(false);
//             return true;
//         }

//     }
//     return ({ login,  error, isLoading });
// };



// LULU'S FIRBASE SETUP
import { useState } from 'react';
import { useAuthContext } from './useAuthContext.jsx';
import { signInWithEmailAndPassword } from 'firebase/auth';
// import { auth } from '../firebaseConfig'; // Import the Firebase Auth instance
import { auth, db } from '../firebaseConfig'; // import your initialized Firebase auth and db


export const useLogin = () => {
    const [error, setError] = useState(null);
    const [isLoading, setIsLoading] = useState(false);
    const { dispatch } = useAuthContext();

    const login = async (email, password) => {
        setIsLoading(true);
        setError(null);

        try {
            const userCredential = await signInWithEmailAndPassword(auth, email, password);
            const user = userCredential.user;

            // Save user details in local storage
            localStorage.setItem('user', JSON.stringify(user));

            // Update auth context
            dispatch({ type: 'LOGIN', payload: user });
            setIsLoading(false);
            return true;
        } catch (err) {
            setError(err.message);
            setIsLoading(false);
            return false;
        }
    };

    return { login, error, isLoading };
};













// // KRIS FIREBASE SETUP
// import { useState } from "react";

// export const useLogin = () => {
//   const [error, setError] = useState(null);
//   const [isLoading, setIsLoading] = useState(false);

//   const login = async (email, password) => {
//     setIsLoading(true);
//     setError(null);

//     try {
//       // Replace with your backend URL
//       const response = await fetch("https://act-production-5e24.up.railway.app/api/auth/login", {
//         method: "POST",
//         headers: {
//           "Content-Type": "application/json",
//         },
//         body: JSON.stringify({ email, password }),
//       });

//       if (!response.ok) {
//         throw new Error("Login failed");
//       }

//       const data = await response.json();
//       localStorage.setItem("token", data.token);

//       setIsLoading(false);
//       return true;
//     } catch (error) {
//       setIsLoading(false);
//       setError(error.message);
//       return false;
//     }
//   };

//   return { login, error, isLoading };
// };



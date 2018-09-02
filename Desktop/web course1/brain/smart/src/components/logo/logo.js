import React from 'react';
import Tilt from 'react-tilt';
import './logo.css';
import brain from './thinking.png';


const Logo = () => {
	return (
		<div style={{display: 'flex', justifyContent: 'center'}}>
			<Tilt className="Tilt" options={{ max : 125 }} style={{ height: 125, width: 125}} >
			 	<div className="Tilt-inner">
			 		<img className={{/*paddingTop: '5px'*/}} alt='logo' src={brain}/>
			 	</div>
			</Tilt>
		</div>
	);
}

export default Logo;
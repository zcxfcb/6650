

import React from 'react';
import './faceRecognition.css'

const faceRecognition= ({url, faceBox}) => {
	return (
		<div className='center ma'>
			<div className='absolute mt2'>
				<img id='urlImage' alt='' src={url} width='300px' heigh='auto'/>
				<div className='face' style={{top: faceBox.to, right: faceBox.ri, bottom: faceBox.bo, left: faceBox.le}}></div>
			</div>
		</div>
	);
}

export default faceRecognition;
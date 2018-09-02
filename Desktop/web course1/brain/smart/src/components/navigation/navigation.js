import React from 'react';

const Navigation = ({onRouteChange, route}) => {
	return (
		<div>
			<nav style={{display: 'flex', justifyContent: 'flex-end'}}>
				{
		          route === 'home' 
		          ? <p onClick={() => onRouteChange('signIn')} className='f5 link dim black underline pa3 pointer'>{'Sign Out'}</p>
		          : <div style={{display: 'flex', justifyContent: 'flex-end'}}>
			          	<p onClick={() => onRouteChange('signIn')} className='f5 link dim black underline pa3 pointer'>{'Sign In'}</p>
			            <p onClick={() => onRouteChange('register')} className='f5 link dim black underline pa3 pointer'>{'Register'}</p>
		            </div>
		        }
			</nav>
		</div>
	);
}

export default Navigation;
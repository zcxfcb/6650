import React from 'react';


const Rank = ({user}) => {
	return (
		<div><br/>
			<div className='f4 yellow'>
				{user.name}{', your number of submitted photo'}
			</div>
			<div className='f2 yellow'>
				{'#'}{user.entries}
			</div>
		</div>
	);
}

export default Rank;
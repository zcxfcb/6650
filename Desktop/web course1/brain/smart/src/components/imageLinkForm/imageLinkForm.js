import React from 'react';
import './imageLinkForm.css'

// You can also use the SDK by adding this script to your HTML:


class ImageLinkForm extends React.Component {
	updateEntries = () => {
		const {user} = this.props;
		fetch(
			'https://shielded-oasis-41179.herokuapp.com/image',
			{
				method: 'put',
				headers: {'Content-Type': 'application/json'},
				body: JSON.stringify({
					id: user.id,
					}
				)
			}
		).then(response => response.json()
		).then(data => {
			console.log(data)
			if (data.id > 0) {
				this.props.updateLocalEntries();
			} else {
				console.log('cannot find user');
			}
		})
	}

	submitAndUpdateEntries = () => {
			this.props.onButtonSubmit(); 
			this.updateEntries();
		}

	render() {
		const {onUrlChange} = this.props;
		return (
			<div>
				<p className='f3'>
					{'This magic brain detects faces in your image'} <br/>
					{'Please key in image url and try!'}
				</p>
				<div className='form center'>
					<div className='form pa2 br2 center shadow-5'>
						<input 
							className='pa2 f4 w-70 center' 
							type='tex'
							onChange={onUrlChange} 
						/>
						<botton 
							className='w-30 grow f4 link ph3 pv2 ba dib white bg-light-purple'
							onClick={this.submitAndUpdateEntries}
						>{'Detect'}
						</botton>
					</div>
				</div>
				<div>
					{this.props.lastInput ? null : <p className="f7 fw6 dark-red" >Unable to find image with the link</p>}
				</div>
			</div>
		);
	}
}

export default ImageLinkForm;
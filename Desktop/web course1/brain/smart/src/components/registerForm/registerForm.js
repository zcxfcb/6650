import React from 'react';
// import './sigxnInForm.css'

// You can also use the SDK by adding this script to your HTML:

class RegisterForm extends React.Component {
	constructor(props) {
		super(props);
		this.state = {
			inputName: '',
			inputEmail: '',
			inputPassword: '',
			isExisted: false,
		}
	}

	onNameChange = (event) => {
		this.setState({inputName: event.target.value});
	}

	onEmailChange = (event) => {
		this.setState({inputEmail: event.target.value});
	}

	onPasswordChange = (event) => {
		this.setState({inputPassword: event.target.value});
	}

	onSignInChange = () => {
		fetch(
			'https://shielded-oasis-41179.herokuapp.com/register',
			{
				method: 'post',
				headers: {'Content-Type': 'application/json'},
				body: JSON.stringify({
					name: this.state.inputName,
					email: this.state.inputEmail,
					password: this.state.inputPassword
					}
				)
			}
		).then(response => response.json()
		).then(data => {
			if (data.email === this.state.inputEmail) {
				this.props.loadUser(data);
				this.props.onRouteChange('home');
			} else {
				this.setState({isExisted: true});
			}
		})

		
	}

	render() {
		return (
			<div>
			<article className="mw6 center bg-transparent br3 pa3 pa4-ns mv3 ba b--black-10 shadow-3">
				<main className="pa4 black-80">
				  <form className="measure">
				    <fieldset id="sign_up" className="ba b--transparent ph0 mh0">
				      <legend className="f4 fw6 ph0 mh0">Register</legend>
				      <div className="">
				        <label className="db fw6 lh-copy f6" htmlFor="email-address">Name</label>
				        <input 
				        	className="pa2 input-reset ba bg-transparent hover-bg-black hover-white w-100" 
				        	type="text" 
				        	name="name"  
				        	id="name" 
				        	onChange ={this.onNameChange}
				        />
				      </div>
				      <div className="mt3">
				        <label className="db fw6 lh-copy f6" htmlFor="email-address">Email</label>
				        <input 
				        	className="pa2 input-reset ba bg-transparent hover-bg-black hover-white w-100" 
				        	type="email" 
				        	name="email-address"  
				        	id="email-address" 
				        	onChange ={this.onEmailChange}
				        />
				      </div>
				      <div className="mv3">
				        <label className="db fw6 lh-copy f6" htmlFor="password">Password</label>
				        <input 
				        	className="b pa2 input-reset ba bg-transparent hover-bg-black hover-white w-100" 
				        	type="password" 
				        	name="password"  
				        	id="password"
				        	onChange = {this.onPasswordChange}
				        />
				      </div>
				    </fieldset>
				    <div className="">
				      <botton 
					      className="b ph3 pv2 input-reset ba b--black bg-transparent grow pointer f6 dib" 
					      onClick = {this.onSignInChange}
				      >Register</botton>
				      <div>
				      	{this.state.isExisted ? null : <p className="f7 fw6 dark-red" >User existed</p>}
				      </div>
				    </div>
				  </form>
				</main>
			</article>
			</div>
		);
	}
}

export default RegisterForm;
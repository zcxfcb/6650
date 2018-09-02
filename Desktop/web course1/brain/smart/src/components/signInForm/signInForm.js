import React from 'react';
// import './sigxnInForm.css'

// You can also use the SDK by adding this script to your HTML:

class SignInForm extends React.Component {
	constructor(props) {
		super(props);
		this.state = {
			inputEmail: '',
			inputPassword: '',
			lastInput: true,
		}
	}

	onEmailChange = (event) => {
		this.setState({inputEmail: event.target.value});
	}

	onPasswordChange = (event) => {
		this.setState({inputPassword: event.target.value});
	}

	onSignInChange = () => {
					console.log('in');

		fetch(
			'https://shielded-oasis-41179.herokuapp.com/signin',
			{
				method: 'post',
				headers: {'Content-Type': 'application/json'},
				body: JSON.stringify({
					email: this.state.inputEmail,
					password: this.state.inputPassword
					}
				)
			}
		).then(response => response.json()
		).then(data => {
			console.log(data);
			if (data.id > 0) {
				this.props.loadUser(data);
				this.props.onRouteChange('home');
			} else {
				this.setState({lastInput: false});
			}
		})
	}

	render() {
		const {onRouteChange} = this.props;
		return (
			<div>
			<article className="mw6 center bg-transparent br3 pa3 pa4-ns mv3 ba b--black-10 shadow-3">
				<main className="pa4 black-80">
				  <form className="measure">
				    <fieldset id="sign_up" className="ba b--transparent ph0 mh0">
				      <legend className="f4 fw6 ph0 mh0">Sign In</legend>
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
				      <label className="pa0 ma0 lh-copy f6 pointer"><input type="checkbox" /> Remember me</label>
				    </fieldset>
				    <div className="">
				      <botton 
					      className="b ph3 pv2 input-reset ba b--black bg-transparent grow pointer f6 dib" 
					      onClick = {this.onSignInChange}
				      >Sign In</botton>
				      <div>
				      	{this.state.lastInput ? null : <p className="f7 fw6 dark-red" >Incorrect Password or Email</p>}
				      </div>
				    </div>
				    <div className="lh-copy mt3">
				      <a href="#0" className="f6 link dim black db" onClick={() => onRouteChange('register')}>Sign up</a>
				      <a href="#0" className="f6 link dim black db">Forgot your password?</a>
				    </div>
				  </form>
				</main>
			</article>
			</div>
		);
	}
}

export default SignInForm;
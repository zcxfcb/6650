import React, { Component } from 'react';
import './App.css';
import Navigation from './components/navigation/navigation';
import Logo from './components/logo/logo';
import ImageLinkForm from './components/imageLinkForm/imageLinkForm.js';
import Rank from './components/rank/rank';
import Particles from 'react-particles-js';
import FaceRecognition from './components/faceRecognition/faceRecognition';
import SignInForm from './components/signInForm/signInForm';
import RegisterForm from './components/registerForm/registerForm';


// The JavaScript client works in both Node.js and the browser.
// Install the client from NPM
// Require the client
// initialize with your api key. This will also work in your browser via http://browserify.org/



const particleOption = {
  particles: {
    number: {
      value: 200
    },
  },
}

const initialState = {
  input: '',
  url: '',
  box: {},
  route: 'signIn',
  user: {
      id: '',
      name: '',
      email: '',
      entries: 0,
      joined: '',
  }
}

class App extends Component {
    constructor() {
      super();
      this.state = {
        input: '',
        url: '',
        box: {},
        route: 'home',
        lastInput: true,
        user: {
            id: '',
            name: '',
            email: '',
            entries: 0,
            joined: '',
        }
      }
    }

    // componentDidMount() {
    //   fetch('http://localhost:3000/')
    //     .then(response => response.json())
    //     .then(console.log)
    // }
    updateLocalEntries = () => {
      this.setState({entries: (this.state.user.entries++)});
    }

    loadUser = (data) => {
      this.setState({user: data});
    }

    onUrlChange = (event) => {
      this.setState({input: event.target.value})
    }

    calculateFaceLocation = (data) => {
      const boundries= data.outputs[0].data.regions[0].region_info.bounding_box;
      const image = document.getElementById('urlImage');
      const x = image.width;
      const y = image.height;
      const param = {
        le: boundries.left_col * x,
        to: boundries.top_row * y,
        ri: (1 - boundries.right_col) * x,
        bo: (1 - boundries.bottom_row) * y,
      };
      this.setState({box: param});
    }

    onButtonSubmit = () => {
      this.setState({url: this.state.input});
      this.setState({lastInput: false});
      console.log("urlsent:",this.state.input);
      fetch(
      'https://shielded-oasis-41179.herokuapp.com/imageAPI',
      {
        method: 'post',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
          input: this.state.input,
          }
        )
      }
      ).then(response => response.json())// app.models.predict("a403429f2ddf4b49b307e318f00e528b", this.state.input)
      .then(data => {
        console.log('data:', data)
          this.calculateFaceLocation(data);
          this.setState({lastInput: true});
      })
      .catch(err => {console.log('err')})
    }

    onRouteChange = (route) => {
      this.setState({route: route});
      if (route === 'signIn') {
        this.setState(initialState)
      }
    }

    render() {
    return (
      <div className="App">
        <Particles className='particles' 
            params={particleOption}
        />
        <Navigation onRouteChange={this.onRouteChange} route={this.state.route}/>
        {
          this.state.route === 'signIn' 
          ? <SignInForm loadUser={this.loadUser} onRouteChange={this.onRouteChange}/>
          : (this.state.route === 'register'
          ? <RegisterForm loadUser={this.loadUser} onRouteChange={this.onRouteChange}/>
          : <div>
              <Logo />
              <Rank user={this.state.user}/>
              <ImageLinkForm  lastInput={this.state.lastInput} user={this.state.user} updateLocalEntries={this.updateLocalEntries} onUrlChange={this.onUrlChange} onButtonSubmit={this.onButtonSubmit}/>
              <FaceRecognition url={this.state.url} faceBox={this.state.box}/>
            </div>)
        }
      </div>
    );
  }
}

export default App;

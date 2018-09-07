import React, { Component, PropTypes } from 'react';
import { Keyboard, KeyboardAvoidingView, StyleSheet,NavigatorIOS, Text, TextInput, View, Button, TouchableHighlight, TouchableOpacity, ScrollView, findNodeHandle, DatePickerIOS} from 'react-native';
import TextInputState from 'react-native/lib/TextInputState'
import SubTasks from './SubTasks'
import DateTimePicker from 'react-native-modal-datetime-picker';
import DatePicker from 'react-native-datepicker'
import CheckBoxGroup from 'react-native-checkbox-group'
import {KeyboardAwareScrollView} from 'react-native-keyboard-aware-scroll-view'

export default class Tasks extends React.Component {
  state = {
  	isDateTimePickerVisible: false,
  };

  constructor(props) {
    super(props);
    // this.state = {text: ''};
    this.state = {
    	textInput: [],    	
    	date:"",
    	viewSection: false

    } 
  }

  renderBottomComponent() {
  	if(this.state.viewSection) {
  		return (
			<View style = {{flexDirection: 'row', alignSelf: 'flex-end'}}>
	      		<CheckBoxGroup
	          		iconSize={40}
	          		checkedIcon="ios-checkbox-outline"
	          		uncheckedIcon="ios-square-outline"
	          		rowStyle={{ flexDirection: 'row'}}
	          		rowDirection={"column"}
	          		labelStyle = {{color: '#333'}}
	          		checkboxes={[
	          			{
	          				label:''
	      				}
	  				]}
	      		/>
              
	            <TextInput 
	              style= {styles.itemText}
	              placeholder="Add Text"
	              onChangeText={(text) => this.setState({text})}
	              // returnKeyType ='next'
	              blurOnSubmit={false}
	              onSubmitEditing = {() => focusTextInput(this.refs.inputB)}
	              // multiline= {true}
	              selectionColor= 'gold'
	              enablesReturnKeyAutomatically = {true}
	              keyboardAppearance = 'dark'
	            />

	            <Button 
	              title= 'Details'
	              style = {styles.subTasks}
	              color = 'blue'
	              onPress= {() => this._navigate(nextScreen)}>
	              styleDisabled = {{color: 'red'}}
	            </Button>
          	</View>
		)
  	}
  }

  _showDateTimePicker = () => this.setState({ isDateTimePickerVisible: true});
  _hideDateTimePicker = () => this.setState({ isDateTimePickerVisible: false});
  _handleDatePicked = (date) => {
  	console.log("Date picked: ", date);
  	this._hideDateTimePicker();
  };
  

  addTextInput = () => {
    this.setState({viewSection: true})
    console.log("Should be adding text input")
  }

  _navigate = (nextScreen) => {
    this.props.navigator.push(nextScreen);
    };
  
  render() {
    return (
      <ScrollView>
      	<View style = {{flex: 1}}>
      		<View>
	      		<TouchableOpacity onPress = {this.addTextInput}>
	      			<Text>Add New Item</Text>
	  			</TouchableOpacity>
	  			{this.renderBottomComponent()}
			</View>
      		<Text style = {{color : 'red'}}>Hi</Text>
      		
		</View>
	  </ScrollView>
    );
  }
}


const styles = StyleSheet.create({
  itemsContainer: {
    flex: 1,
    backgroundColor: 'skyblue',
    alignItems: 'center',
    paddingTop: 15
  },
  headerText: {
    color: 'black',
    fontSize: 30,
    paddingTop: 20,
    alignItems: 'center',
    paddingBottom: 20
  },
  sectionText: {
    fontSize: 20,
    },
  itemText: {
    height: 45,
    flex: 9,
    fontSize: 16
  },
  date: {
  	fontSize: 18, 
  	marginTop: 2, 
  	color: 'green'
  },
  subTasks: {
  	fontSize: 18, 
  	alignItems: 'flex-end',
  }
});

export function focusTextInput(node) {
  try {
    TextInputState.focusTextInput(findNodeHandle(node))
  } catch (e) {
    console.log("Couldn't focus text input: ", e.message)
  }
}

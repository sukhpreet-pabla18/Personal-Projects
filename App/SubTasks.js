import React, { Component, PropTypes } from 'react';
import { StyleSheet,NavigatorIOS, Text, TextInput, View, Button, TouchableHighlight, TouchableOpacity, ScrollView, findNodeHandle, DatePickerIOS} from 'react-native';
import TextInputState from 'react-native/lib/TextInputState'
import DatePicker from 'react-native-datepicker'


export default class SubTasks extends React.Component {
  constructor(props) {
    super(props);
    // this.state = {text: ''};
    this.state = {textInput: [],
      date: new Date(),
    } 
  }

  addTextInput = (key) => {
    let textInput = this.state.textInput;
    textInput.push(<TextInput key = {key} />);
    this.setState({ textInput})
    console.log("Should be adding text input")
  }

  onDateChange(date) {
    this.setState({date: date});
  }

  render() {
    return (
      <ScrollView>
        <DatePicker
              style= {{width: 80, height: 20, left: 10}}
              date= {this.state.date}
              mode="date"
              placeholder="Select date"
              format="MM/DD/YY"
              confirmBtnText = "Confirm"
              cancelBtnText="Cancel"
              onDateChange = {(date) => {this.setState({date: date})}}
              showIcon = {false}
              customStyles= {{
                dateInput: {

                }
              }}
          />
        <View style = {{backgroundColor: 'skyblue'}}>
          <View style = {{alignItems: 'center'}}>
            <Text style = {styles.headerText}>To-Do List</Text>
          </View>
        </View>

        <View style = {{flexDirection: 'row', justifyContent: 'center', backgroundColor: 'skyblue'}}>
          
          <Text style = {styles.sectionText}>Today</Text>
          <View>
            <Button title='+' onPress={() => this.addTextInput(this.state.textInput.length)} />
            {this.state.textInput.map((value, index) => {
              return value
            })}
          </View>
        </View>

        <View style={styles.itemsContainer}>
          <View style = {{flexDirection: 'row'}}>
            <TextInput 
              style= {styles.itemText}
              placeholder="Item 1"
              onChangeText={(text) => this.setState({text})}
              returnKeyType ='next'
              blurOnSubmit={false}
              onSubmitEditing = {() => focusTextInput(this.refs.inputB)}
            />
          
          </View>

          <View style = {{flexDirection: 'row'}}>
            <TextInput 
              style= {styles.itemText}
              placeholder="Item 2"
              onChangeText={(text) => this.setState({text})}
              ref = 'inputB'
              returnKeyType ='next'
              blurOnSubmit={false}
              onSubmitEditing = {() => focusTextInput(this.refs.inputC)}
            />
            
          </View>

          <View style = {{flexDirection: 'row'}}>
            <TextInput 
              style= {styles.itemText}
              placeholder="Item 3"
              onChangeText={(text) => this.setState({text})}   
              ref = "inputC"
              returnKeyType ='next'
              blurOnSubmit={false}
              onSubmitEditing = {() => focusTextInput(this.refs.inputD)}
            />
            
          </View>

          <View style = {{flexDirection: 'row'}}>
            <TextInput 
              style= {styles.itemText}
              placeholder="Item 4"
              onChangeText={(text) => this.setState({text})}
              ref = "inputD"
              returnKeyType ='next'
              blurOnSubmit={false}
              onSubmitEditing = {() => focusTextInput(this.refs.inputE)}
            />
            
          </View>

          <View style = {{flexDirection: 'row'}}>
            <TextInput 
              style= {styles.itemText}
              placeholder="Item 5"
              onChangeText={(text) => this.setState({text})}
              ref = "inputE"
              returnKeyType = 'next'
              // blurOnSubmit={false}
              // onSubmitEditing = {() => focusTextInput(this.refs.inputF)}
            />
          </View>
          
        </View>

        <View style = {styles.itemsContainer}>
          <Text style = {styles.sectionText}>Tomorrow</Text>
          <TextInput 
            style= {styles.itemText}
            placeholder="Item 1"
            onChangeText={(text) => this.setState({text})}
          />
          <TextInput
            style= {styles.itemText}
            placeholder="Item 2"
            onChangeText={(text) => this.setState({text})}
          />
          <TextInput
            style= {styles.itemText}
            placeholder="Item 3"
            onChangeText={(text) => this.setState({text})}
          />
          <TextInput
            style= {styles.itemText}
            placeholder="Item 4"
            onChangeText={(text) => this.setState({text})}
          />  
          <TextInput
            style= {styles.itemText}
            placeholder="Item 5"
            onChangeText={(text) => this.setState({text})}
          />
        </View>

        <View style = {styles.itemsContainer}>
          <Text style = {styles.sectionText}>Later</Text>
          <TextInput 
            style= {styles.itemText}
            placeholder="Item 1"
            onChangeText={(text) => this.setState({text})}
          />
          <TextInput
            style= {styles.itemText}
            placeholder="Item 2"
            onChangeText={(text) => this.setState({text})}
          />
          <TextInput
            style= {styles.itemText}
            placeholder="Item 3"
            onChangeText={(text) => this.setState({text})}
          />
          <TextInput
            style= {styles.itemText}
            placeholder="Item 4"
            onChangeText={(text) => this.setState({text})}
          />  
          <TextInput
            style= {styles.itemText}
            placeholder="Item 5"
            onChangeText={(text) => this.setState({text})}
          />
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
    height: 40,
    left: 10,
    flex: 2,
    fontSize: 18
  }
});

export function focusTextInput(node) {
  try {
    TextInputState.focusTextInput(findNodeHandle(node))
  } catch (e) {
    console.log("Couldn't focus text input: ", e.message)
  }
}

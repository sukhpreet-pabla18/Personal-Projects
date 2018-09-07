import React, { Component, PropTypes } from 'react';
import { StyleSheet,NavigatorIOS, Text, TextInput, View, Button, TouchableHighlight, TouchableOpacity, ScrollView, findNodeHandle, DatePickerIOS} from 'react-native';
import TextInputState from 'react-native/lib/TextInputState';
import Tasks from './Tasks';
import * as firebase from 'firebase';

export default class App extends React.Component {
  render() {
    return (
    <NavigatorIOS
      initialRoute = {{
        title: 'To-Do List',
        component: Tasks
      }}
      style = {{ flex: 1}}
      barTintColor = 'gold'
      tintColor = 'darkblue'
      titleTextColor = 'darkblue'
    /> 
    );
  }
}


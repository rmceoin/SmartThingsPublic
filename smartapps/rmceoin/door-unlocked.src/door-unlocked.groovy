/**
 *  Door Unlocked
 *
 *  Copyright 2017 Randy McEoin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
import groovy.json.JsonSlurper

definition(
    name: "Door Unlocked",
    namespace: "rmceoin",
    author: "Randy McEoin",
    description: "Actions for when a door is unlocked",
    category: "Safety & Security",
    iconUrl: "http://cdn.device-icons.smartthings.com/Home/home3-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Home/home3-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Home/home3-icn@3x.png")


preferences {
    section("Door is unlocked") {
        input (name: "thelock", type: "capability.lock", required: true, title: "Tap to set")
    }
    section("Optional trigger requirements") {
        input(name: "code", type: "number", required: false, title: "Code used")
        input(name: "modes", type: "mode", required: false, multiple: true, title: "Only during these mode(s)")
    }
    section("Actions") {
        input (name: "notify", type: "bool", required: true, defaultValue: "false", title: "Notify when unlocked")
        input (name: "theswitch", type: "capability.switch", required: false, title: "Turn on switch")
    }}

def installed() {
//  log.debug "Installed with settings: ${settings}"
//  log.debug "Has the code: ${code}"

	initialize()
}

def updated() {
//  log.debug "Updated with settings: ${settings}"
//  log.debug "Watching for code: ${code} - modes: ${modes}"

	unsubscribe()
	initialize()
}

def initialize() {
    subscribe(thelock, "lock", lockHandler)
}

def lockHandler(evt) {
	if (evt.value == "unlocked") {
        def data = new JsonSlurper().parseText(evt.data)
        if ((data == null) || (data.lockName == null)) {
            log.debug "data was null or data.lockName was null: ${data}"
            return
        }
        log.debug "data: ${data}"
        //
        // Example data is:
        // data: [codeName:Pet sitter, usedCode:2, method:keypad, lockName:Side Door]
        //
        if ((modes != null) && (!modes.contains(location.mode))) {
            return
        }
        def usedCode = -1
        if (data.containsKey("usedCode")) {
            usedCode = data.usedCode as Integer
        }
        if ( (code != null) && (code != usedCode)) {
            log.debug "Code ${code} does not match ${usedCode}"
            return
        }
        if (notify) {
            if (usedCode > -1) {
                sendPush("${data.lockName} was unlocked with code ${usedCode}")
            } else {
                sendPush("${data.lockName} was unlocked")
            }
        } else {
            log.debug "Notify is disabled"
        }
        if (theswitch != null) {
            theswitch.on()
        } else {
            log.debug "No switch specified"
        }

    }
}


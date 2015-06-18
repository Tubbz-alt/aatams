[![Build Status](https://travis-ci.org/aodn/aatams.png?branch=master)](https://travis-ci.org/aodn/aatams)

## Overview
The Australian Animal Tagging and Monitoring System (AATAMS) is one of eleven facilities of the Integrated Marine Observing System (IMOS). AATAMS represents the higher biological monitoring of the marine environment for the IMOS program.

This repository is for the web application which supports the management of AATAMS data.

## Application Features

### Upload and Storage
The web app enables the *upload* and *storage* of AATAMS data, which can be broadly categorised as:

* detection and event data
  * downloaded from receivers and uploaded (as CSV files) to the app
* metadata
  * entered manually through a web UI
  * information about people, projects and organisations
  * information about receivers and when and where they are deployed
  * infromation about tags and when, where and to what species they are deployed

### Access
The web application also gives the following *access* to the data:

* a web UI, with list and individual item views of all the data
* searching and filtering of data
* selected CSV and PDF file downloads
* PDF summary reports

### Security
At all times, a number of security rules are enforced, restricting who can *upload* data, and what people can *access*.

### Notifications
Email notifications are sent to users in the following cases:

* visibility of user data has changed or is soon about to change
* new data has been uploaded (by another user) which is related to a user's own data


## High Level Domain Concepts and Design
See [doc/DESIGN.md](doc/DESIGN.md).

## Development

### Development Environment
See [doc/CONTRIBUTING.md](doc/CONTRIBUTING.md).

### Restoring the AATAMS database
See [doc/BACKUP_AND_RESTORE.md](doc/BACKUP_AND_RESTORE.md).

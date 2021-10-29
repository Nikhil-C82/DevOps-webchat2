# WebChatApplication

Repository of Gennady Trubach (2d course 5th group) for studying practise in Belarussian State University.

## How to get this project in Intellij IDEA

1. Go to File->New->Project from exiting sources.
2. Then choose Import project from external model->Maven.
3. Next choose some settings and finish importing the project.

## How to add local Tomcat server in Intellij IDEA

1. Go to Run->Edit Configurations.
2. Add New Configuration->Tomcat Server->Local.
3. Go to Run->Edit Configurations->Deployment.
4. Add artifact and change application context (or war files decompressed into ROOT folder in Tomcat->webapps).

## How to create markdown files

You can use [dillinger.io](http://dillinger.io/). For learn how to write in markdown you can read https://help.github.com/articles/markdown-basics/.

## Current state of project

* Task 10 :
The xml history is stored in your home directory. Please, before running server, put file history.xml to your home directory (to know your home directory open command promt and paste `echo %USERPROFILE%`).  
For writing history to xml used DOM parser, for parsing from xml used STAX parser.
* Task 11 :
Now editing and deleting messages are available. How to see web exceptions (400, 404, 500) in your browser : 
    * 400 - Write /WebChatApplication/chat in url  
    (f. e. `http://localhost:8080/WebChatApplication/chat`)
    * 404 - Write after mask /WebChatApplication/ something wrong  
    (f. e. `http://localhost:8080/WebChatApplication/hub`)
    * 500 - Write after mask /WebChatApplication/ something wrong  
    (f. e. `http://localhost:8080/WebChatApplication/chat?token=XXX`)
* Task 12 :
In this version used ajax polling and cache technology.
* Task 12* :
Long polling version of the project. Requests rewrited with jQuery lib. Also chat has clever scrolling now.
* Task 13 :
MySQL data base created.
* **Task 14** :
This is the stable version of the project. All messages and users are stored in databases. Also add improvement with logging in special database.

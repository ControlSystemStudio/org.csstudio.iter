'''
Created on Nov 18, 2014

@author: bobnarj

13.9.2017 - Borut Terpinc
Added the text update test 
'''

from pyOlog import Logbook, LogEntry, OlogClient, Attachment
from datetime import datetime
import time, os

if __name__ == '__main__':
    ologurl = 'http://localhost:8082/Olog'
    ologUsername = 'anonymous'
    ologPassword = '$anonymous'
    
    client = OlogClient(url=ologurl,username=ologUsername,password=ologPassword)
    
    ''' List all logbooks that currently exist in elog '''
    def listLogbooks():
        print('Retrieving the logbooks from ' + ologurl)
        logs = client.listLogbooks()
        print('  Logbooks:')
        for s in logs:
            print('    ' + s.getName())
        
    ''' Create a new entry containing a specific text in the logbook CODAC '''    
    def createTextEntry():
        lb = Logbook(name='CODAC')
        print('Creating a text entry in the logbook ' + lb.getName())
        text = 'This is a demo python log entry created on ' + datetime.now().isoformat(' ')
        '''owner doesn't play any role here but it is required'''
        testLog = LogEntry(text=text, owner='owner', logbooks=[lb]) 
        client.log(logEntry=testLog)
    
    ''' Create a new entry containing a specific text in all logbooks '''     
    def createTextEntryMultipleLogbooks():
        logs = client.listLogbooks()
        print('Creating a text entry in all logbooks')
        text = 'This is a demo python log entry created on ' + datetime.now().isoformat(' ')
        testLog = LogEntry(text=text, owner='owner', logbooks=logs) 
        client.log(logEntry=testLog)
        
    ''' Create a new entry in the logbook CODAC and use the first tag from the list of available tags '''    
    def createEntryWithTags():
        lb = Logbook(name='CODAC')
        tags = client.listTags()
        print('Creating a text entry with a tag ' + tags[0].getName() + ' in the logbook ' + lb.getName())     
        text = 'This is a demo python log entry created on ' + datetime.now().isoformat(' ')
        testLog = LogEntry(text=text, owner='owner', logbooks=[lb], tags=[tags[0]]) 
        client.log(logEntry=testLog)
        
    ''' Create a new entry in logbook CODAC and attach an image to that entry '''
    def createEntryWithAttachment():
        lb = Logbook(name='CODAC')
        print('Creating a text entry with an attached image in the logbook ' + lb.getName())     
        text = 'This is a demo python log entry  wtih image attachment created on ' + datetime.now().isoformat(' ')
        pp = os.path.dirname(os.path.abspath(__file__))
        path = os.path.relpath('Desert.png', pp)
        image = Attachment(open(path,'rb'))
        testLog = LogEntry(text=text, owner='owner', logbooks=[lb], attachments=[image]) 
        client.log(logEntry=testLog)
        
    ''' Search for all entries (in all logbooks) that contain the word \'demo\' and have been made in the last hour'''    
    def searchForLastHourEntries():
        startTime = str(time.time()-3600).split('.')[0]
        endTime = str(time.time()).split('.')[0]
        print('Entries created in the last hour and containing \'demo\'')
        logs = client.find(search='*demo*',start=startTime, end=endTime)
        for s in logs:
            print(s.getText())
            
        print('Entries created in the last hour with tag \'Design\'')
        logs = client.find(search='*',start=startTime, end=endTime, tag='Design')
        for s in logs:
            print(s.getText())

    '''Update log entry with image attachment '''        
    def updateLogEntry():
        lb = Logbook(name='CODAC')
        print('Creating a log entry which will be updated in logbook ' + lb.getName())         
        text = 'This is a demo python log entry which will be updated, created on ' + datetime.now().isoformat(' ')
        testLog = LogEntry(text=text, owner='owner', logbooks=[lb]) 
        log = client.log(logEntry=testLog)

        textupdate = '<br>And this is update text to python log entry ' + datetime.now().isoformat(' ')
        updatedLogEntry = LogEntry(text=text + textupdate, owner='owner', logbooks=[lb]) 
        updatedLog = client.update(log.getId(), updatedLogEntry)
        logEntries = client.find(search=text)
        
'''Here update methods are called. You can commnet out the desired methods '''
'''
listLogbooks()    
print('')
createTextEntry()
print('')
createTextEntryMultipleLogbooks()
print('')
createEntryWithTags()
print('')
createEntryWithAttachment()
print('')
updateLogEntry()
'''
print('')
searchForLastHourEntries()

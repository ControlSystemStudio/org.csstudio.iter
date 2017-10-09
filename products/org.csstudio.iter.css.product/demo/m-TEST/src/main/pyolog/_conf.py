# -*- coding: utf-8 -*-
"""
Internal module

Used to read the pyOlog.conf file

example file
cat ~/pyOlog.conf
[DEFAULT]
url=http://localhost:8000/Olog
"""

def __loadConfig():
    import os.path
    import ConfigParser
    dflt={'url':'http://localhost:8000/Olog'}
    cf=ConfigParser.SafeConfigParser(defaults=dflt)
    cf.read([
        '/etc/pyOlog.conf',
        os.path.expanduser('~/pyOlog.conf'),
        'pyOlog.conf', 
        '/opt/codac/lib/python/site-packages/pyOlog.conf'
    ])
    return cf

_conf=__loadConfig()
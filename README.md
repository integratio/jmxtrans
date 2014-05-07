This is a fork of the [JMXTrans project](https://github.com/jmxtrans/jmxtrans)

The difference from the original project:

 - ElasticSearch OutputWriter
	- Tested against ElasticSearch 1.0.1
	- Only embedded node client mode is supported
	
Example of configuration file for ElasticSearch OutputWriter
	
```javascript
{
    "servers":[
        {
            "port":"9999",
            "host":"localhost",
            "queries":[
                {
                    "outputWriters":[
                        {
                            "@class":"com.googlecode.jmxtrans.model.output.ElasticSearchWriter",
                            "settings":{
                                "elasticIndexName":"monitor",
                                "elasticTypeName":"heapMemoryUsage"
                            }
                        }
                    ],
                    "obj":"java.lang:type=Memory",
                    "attr":[
                        "HeapMemoryUsage",
                        "NonHeapMemoryUsage"
                    ]
                },
                {
                    "outputWriters":[
                        {
                            "@class":"com.googlecode.jmxtrans.model.output.ElasticSearchWriter",
                            "settings":{
                                "elasticIndexName":"monitor",
                                "elasticTypeName":"cmsOldGen"
                            }
                        }
                    ],
                    "obj":"java.lang:name=CMS Old Gen,type=MemoryPool",
                    "attr":[
                        "Usage"
                    ]
                },
                {
                    "outputWriters":[
                        {
                            "@class":"com.googlecode.jmxtrans.model.output.ElasticSearchWriter",
                            "settings":{
                                "elasticIndexName":"monitor",
                                "elasticTypeName":"lastGcInfo"
                            }
                        }
                    ],
                    "obj":"java.lang:name=ConcurrentMarkSweep,type=GarbageCollector",
                    "attr":[
                        "LastGcInfo"
                    ]
                }
            ],
            "numQueryThreads":2
        }
    ]
}


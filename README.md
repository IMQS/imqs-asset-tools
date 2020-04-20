A command line utility for routing a command to the responsible handler class.

The program expects two arguments:
1. Command : exportBoq, importBoq
2. Config file  : -f "filename", filename of the configuration file.
3. Log file : -l filename of the properties file for log4j

Example:
importBoq -f "C:\dev\Java\IMQS\imqs-asset-tools\src\main\java\za\co\imqs\conf\boq-import-config.json"

Each handler class is responsible for parsing its own configuration file. 

1. boq-export-config.json example:
 {
   "dbParams": {
     "driverClass": "com.microsoft.sqlserver.jdbc.SQLServerDriver",
     "jdbcUrl": "jdbc:sqlserver://localhost:1433;databaseName=IMQS_Asset_BuffaloCity_20180510",
     "username": "sa",
     "password": "password"
   },
   "exportFilename": "D:\\Dump\\IMQS\\Exports\\test\\boq_export.csv"
 }

2. boq-import-config.json example:
 {
 	"imqs-template-service": {
 		"baseURI": "http://192.168.1.65:8668/template/v1_0/",
 		"authSvcURI": "http://192.168.1.65:/auth2/login",
 		"username": "imqs",
 		"password": "password"
        },
 	"batchSize": 5,
 	"importFilename": "D:\\Dump\\IMQS\\Exports\\test\\boq_export.csv",
 	"fieldList": {
 		"eul": "NUMERIC",
 		"extentUnit": "STRING",
 		"eulUnit": "STRING",
 		"eulCG": "NUMERIC",
 		"residualPct": "NUMERIC",
 		"residualPctCG": "NUMERIC",
 		"unitRate": "NUMERIC",
 		"refUnitRateUnit": "STRING",
 		"descriptorSizeUnit": "STRING",
 		"extentConversion": "NUMERIC",
 		"unitRateCG": "NUMERIC",
 		"IncludeDescriptorSize": "BOOLEAN",
 		"CRCCalcIncludeDescriptorSize": "BOOLEAN",
 		"InterpolateDescriptorSize": "STRING",
 		"InterpolationTolerancePercentage": "NUMERIC",
 		"refDepreciationMethodId": "STRING"
    }
 }
 
The fieldlist above in the config lists all the fields in the import file that must be processed by the importer for a template. 

Note - the import file also contains the following fields which are mandatory:
componentType_ID,componentType,
descriptorType_ID,descriptorType,
descriptorClass_ID,descriptorClass,
descriptorSize_ID,descriptorSize,
boqPath
 

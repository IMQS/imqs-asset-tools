A command line utility (Controller class) for routing a request to the responsible handler class.

The program expects two arguments:
1. Command : exportBoq, importBoq
2. Config file  : -f "filename", filename of the configuration file.

Example:
importBoq -f "C:\dev\Java\IMQS\imqs-asset-tools\src\main\java\za\co\imqs\conf\boq-import-config.json"

Each handler class is responsible for parsing its own configuration file. Should a database connection be required,
a "dbParams" key is expected, specifying the connection parameters.

For example:
{
	"dbParams": {
		"driverClass": "com.microsoft.sqlserver.jdbc.SQLServerDriver",
		"jdbcUrl": "jdbc:sqlserver://localhost:1433;databaseName=IMQS_Asset_BuffaloCity_20180510",
		"username": "sa",
		"password": "1mq5p@55w0rd"
	},
	"exportFilename": "D:\\Dump\\IMQS\\Exports\\test\\export_java.csv"
}
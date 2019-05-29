module.exports = {
user : process.env.NODE_ORACLEDB_USER || "admin",
password : process.env.NODE_ORACLEDB_PASSWORD || "password",
connectString : process.env.NODE_ORACLEDB_CONNECTIONSTRING || "service_name",
externalAuth : process.env.NODE_ORACLEDB_EXTERNALAUTH ? true : false
};

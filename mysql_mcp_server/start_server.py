#!/usr/bin/env python3
"""
MySQL MCP Server 启动脚本
用于设置环境变量并启动 mysql_mcp_server
"""
import os
import sys
import asyncio

# 设置环境变量
os.environ["MYSQL_HOST"] = "localhost"
os.environ["MYSQL_PORT"] = "3306"
os.environ["MYSQL_USER"] = "root"
os.environ["MYSQL_PASSWORD"] = "xiong3410313"
os.environ["MYSQL_DATABASE"] = "myliu_flowchart"

# 修复 Windows 上 mysql-connector-python 的兼容性问题
# 使用 pymysql 替代 mysql-connector-python
import pymysql
pymysql.install_as_MySQLdb()

# Monkey-patch mysql.connector 使用 pymysql
import mysql
mysql.connector = pymysql
sys.modules['mysql.connector'] = pymysql

# 导入并运行 mysql_mcp_server
from mysql_mcp_server import main

if __name__ == "__main__":
    main()

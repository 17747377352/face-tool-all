FROM eclipse-temurin:21-jre

WORKDIR /app

# 复制 JAR 文件
COPY target/*.jar app.jar

# 复制配置文件（外部配置）
COPY src/main/resources/application*.yml ./

# 暴露端口
EXPOSE 28089

# 启动应用（使用外部配置文件，按优先级加载）
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=file:./application.yml,file:./application-dev.yml,file:./application-prod.yml"]

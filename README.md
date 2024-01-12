# Projeto Lambda + S3 + LocalStack

# Objetivo

Desenvolvimento de lambdas functions que permitem o acesso a um bucket da S3.</br>
</br> As funções lambda permitirão realizar o upload de arquivos em um diretório específico, 
assim como listar todos os arquivos e realizar o download quando necessário.</br>
</br> Todos os ambientes disponíveis nesse projeto (API Gateway, lambda e S3) estarão rodando localmente, 
recurso permitido através do LocalStack, ferramenta que permite simular todos os ambientes 
da AWS em uma máquina local.

## Build

`./gradlew clean build`

## Docker
O LocalStack é executado através do Docker, sendo assim basta rodar o comando abaixo dentro da pasta
localstack contida nesse repositório. </br>

</br>`docker compose up`

## Deploy
Para facilitar o deploy das funções lambda, estou utilizando o serverless framework, como o nome já
surgere, é um framework voltado pra aplicações serverless.

Com o docker em execução, no terminal, rodar o seguinte comando:


</br>`serverless deploy --stage local`

## Orientações para geração de um projeto do zero

### Instalar o serverless framework

É possível ler mais sobre na própria página do framework: `https://www.serverless.com/`

Rodar o comando, em seu prompt, `npm install -g serverless`

### Criando um projeto

Após instalação, insira o comando `serverless` e o próprio pronto irá guiar a criação de um novo projeto, ou
, caso queira criar um projeto em node ou kotlin, basta seguir os comandos abaixo respectivamente:

`serverless create --template aws-nodejs --path {nome_do_projeto}`</br>
</br>`serverless create --template aws-kotlin-jvm-gradle --path {nome_do_projeto}`

### Instalar plugin do LocalStack para o Serverless Framework

Como a intenção é simular os ambientes da AWS local, ou seja, utilizar o LocalStack, é recomendado
instalar o plugin do LocalStack para o Serverless Framework, basta rodar o comando abaixo:

`npm install --save-dev serverless-localstack`

### Configurando o plugin do LocalStack

No arquivo serverless.yml, deve adicionar a configuração abaixo.

```
plugins:
  - serverless-localstack
custom:
  localstack:
    debug: true
    stages:
      - local
      - dev
```
### Declarando as funções lambdas a serem externalizadas

O template será gerado sem os eventos para "startar" as funções, esse bloco de declaração das funções
é para que seja gerado os endpoints de acesso as funções lambda, então abaixo de cada handler adicione seu evento e método,
conforme exemplo abaixo:

```
functions:
  hello:
    handler: com.serverless.Handler
    events:
      - http:
          path: hello
          method: get
  listFile:
    handler: com.serverless.HandlerList
    events:
      - http:
          path: list-file
          method: get
  uploadFile:
    handler: com.serverless.HandlerUpload
    events:
      - http:
          path: upload-file
          method: post
  justTest:
    handler: com.serverless.JustTest
    events:
      - http:
          path: just-test
          method: get
```

# Observação:
Tive problema para utilizar métodos estáticos com o template default do kotlin, onde ele acusava
que estava usando JDK 6, onde estava configurado a JDK 11. Só consegui corrigir adicionando, no build.gradle,
a config abaixo:

```
compileKotlin {
  kotlinOptions.jvmTarget = "11"
}
```
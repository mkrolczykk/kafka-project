### GitHub accounts analyzer application. <br /> <br />

Main goal of application is to extract users data from GitHub API and put result into kafka. <br /> <br />

**Application scenario**:

1. Get users data from GitHub API for given time interval (see github-accounts.json file) <br />

2. Analyze and extract the data <br />

3. Produce some information about users commits <br />

4. Put everything into kafka for next analyze <br />

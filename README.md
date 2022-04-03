# Web Scraper
A poc to illustrate the web scraping using spring boot

# Start the scrapers 
    - Under the resources directory locate folder named "scrapers"
    - under "scrapers" folder create your json file 
    - In the json file provide the selectors of all the mandatory tags
    - For the mandatory tags look for json.mandatory.fields key in the application.properties file
    - For help refer to test1.json and test2.json

# Test for scraping all the websites 
curl --location --request GET 'http://localhost:8080/api/getData' \
--header 'Authorization: Basic c2E6cGFzc3dvcmQ='


# Test for fetching all websites' data existing in the db
curl --location --request GET 'http://localhost:8080/api/fetchAll' \
--header 'Authorization: Basic c2E6cGFzc3dvcmQ='

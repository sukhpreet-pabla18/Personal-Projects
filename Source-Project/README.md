# SourceProject
Say you're having a discussion with someone and they give you a statistic. How can you verify that quickly and accurately? Well, this is where our project comes in. You can input a question (for example, "How many Americans died in the US in 2016") into the program, and you'll receive a fairly reliable answer. We use Google CustomSearch API to get search results. Then, we use Apache OpenNLP library to help separate the different words/phrases in the query by their parts of speech, which we use to generate a list of the most probable ways that the answer will show up in the Google search results. We use JSoup to scrape each of the web pages, looking for any of the phrases in the list of answer formats. After getting the statistical value from each webpage, the algorithm uses a histogram of the data values and Google PageRank to return the answer to your query. 

## Authors

* **Sukhpreet Pabla** - [Computer Science, UC Berkeley]
* **Sidh Sikka** - [Astrophysics, UCLA]


## Acknowledgments

* Inspiration goes out to our best friends from high school. We have debates all the time about myriad topics, and whenever one side of the debate gives a statistic the other side has to try to quickly skim the first Google search result for a counter-statistic. This project is a way to resolve that pattern for us and for people across the world.  

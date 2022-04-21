from requests_html import HTMLSession
from bs4 import BeautifulSoup
import re
import sys

URL = "https://cobr.ai/tournaments/"
TID = str(sys.argv[1])
EXT = "/rounds"

TOURNEY= URL + TID + EXT
session = HTMLSession()
page = session.get(TOURNEY)
soup = BeautifulSoup(page.content, "html.parser")
rows = soup.find_all('div', attrs={'class':'round_pairing'})

for row in rows:
    left = row.find('div', attrs={'class':'left_player_name'})
    right = row.find('div', attrs={'class':'right_player_name'})
    scores = row.find('div', attrs={'class':'centre_score'}).text

    print(left.contents[0].strip())
    print(scores.split("-")[0].strip())
    
    print(right.contents[0].strip())
    print(scores.split("-")[1].strip())

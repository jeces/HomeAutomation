import requests
import datetime

# xml 파싱하기
import xml.etree.ElementTree as elemTree



#api 파싱 주소
dust_url = "http://openapi.airkorea.or.kr/openapi/services/rest/ArpltnInforInqireSvc/getCtprvnMesureLIst?"
weather_url = "http://apis.data.go.kr/1360000/VilageFcstInfoService/getVilageFcst?"

#dust_apikey
service_key = "XIR%2F7enMmtb%2FRvXmb7Lrc0pZTegsHRd9xv%2Fo0D1j1efyfS%2FAt3u3wAxJ%2FHXLdTY%2B4VPHY%2FkOmEuH8KWmOu8x0A%3D%3D"


#weather
today = datetime.datetime.today()
base_date = today.strftime("%Y%m%d") # 
base_time = "0800" # 날씨 값

#울산 좌표값
nx = "102"
ny = "84"


data = dict()
data['date'] = base_date

weather_data = dict()

payload = "serviceKey=" + service_key + "&" +\
    "dataType=json" + "&" +\
    "base_date=" + base_date + "&" +\
    "base_time=" + base_time + "&" +\
    "nx=" + nx + "&" +\
    "ny=" + ny

res = requests.get(weather_url + payload)
items = res.json().get('response').get('body').get('items')




for item in items['item']:  #습도 데이터
    if item['category'] == 'T3H':
        weather_data = item['fcstValue']


    if item['category'] == 'REH':
        api_humidity = item['fcstValue']



#dust 미세 먼지 pm10은 미세먼지, pm25 는 초미세먼지
item_code_pm10 = "PM10"
item_code_pm25 = "PM25"

data_gubun = "HOUR"
search_condition = "WEEK"

payload = "serviceKey=" + service_key + "&" +\
    "dataType=json" + "&" +\
    "dataGubun=" + data_gubun + "&" +\
    "searchCondition=" + search_condition  + "&" +\
    "itemCode="

# pm10 pm2.5 수치 가져오기
pm10_res = requests.get(dust_url + payload + item_code_pm10)
pm25_res = requests.get(dust_url + payload + item_code_pm25)


pm10_tree = elemTree.fromstring(pm10_res.text)
pm25_tree = elemTree.fromstring(pm25_res.text)

dust_data = dict()
for tree in [pm10_tree, pm25_tree]:
    item = tree.find("body").find("items").find("item")
    code = item.findtext("itemCode")
    value = int(item.findtext("seoul"))

    dust_data[code] = {'value': value}

dust_data





#아이콘으로 표시를 하거나, 다른 곳에 쓸 경우 
# PM10 미세먼지 30 80 150  
pm10_value = dust_data.get('PM10').get('value')
if pm10_value <= 30:
    pm10_state = "좋음"
elif pm10_value <= 80:
    pm10_state = "보통"
elif pm10_value <= 150:
    pm10_state = "나쁨"
else:
    pm10_state = "매우나쁨"


pm25_value = dust_data.get('PM2.5').get('value')
# PM2.5 초미세먼지 15 35 75
if pm25_value <= 15:
    pm25_state = "좋음"
elif pm25_value <= 35:
    pm25_state = "보통"
elif pm25_value <= 75:
    pm25_state = "나쁨"
else:
    pm25_state = "매우나쁨"


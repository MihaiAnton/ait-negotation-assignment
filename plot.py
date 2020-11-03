import matplotlib.pyplot as plt
import json
import re

#change this to your path to JSON log file
path_to_JSON_file = '/Users/stefanpetrescu/DELFT/AITechniques/Plot_Graphs/file.json'

#------------------------------#
#class for groupMeetingProfiles
class Profile:

    #day issue
    __day = '' 
    def __init__(self,day={}):  # constructor
        self.__day = issueWeights
    def set_day_utilities(self,day):        # function
        self.__day = day
    def get_day_utilities(self):
        return self.__day
    def get_days_utilities(self,day_of_week):        # function
        if(day_of_week == 'Monday'):
          return self.__day[0]
        if(day_of_week == 'Tuesday'):
          return self.__day[1]
        if(day_of_week == 'Wednesday'):
          return self.__day[2]
        if(day_of_week == 'Thursday'):
          return self.__day[3]
        if(day_of_week == 'Friday'):
          return self.__day[4]
        #in case of an offer for which no day was specified => 0 element
        if(day_of_week == 0):
          return 0

    __duration = '' 
    __m_duration = '' #f(x) = mx + b -> m 
    __b_duration = '' #f(x) = mx + b -> b
    def __init__(self,duration={}):
        self.__duration = duration
    def set_duration_utilites(self,duration):
        self.__duration = duration
    def get_duration_utilities(self):
        return self.__duration
    def get_durations_utilities(self, duration):
        self.__m_duration = (self.__duration[3] - self.__duration[1]) / (self.__duration[2] - self.__duration[0])
        self.__b_duration = self.__duration[1] - self.__m_duration * self.__duration[0] 
        return abs(self.__m_duration*duration + self.__b_duration)

    __location = '' 
    def __init__(self,location={}):
        self.__location = location
    def set_location_utilites(self,location):
        self.__location = location
    def get_location_utilities(self):
        return self.__location
    def get_locations_utilities(self, location_exact):
        if(location_exact == 'zoom'):
          return self.__location[0]
        if(location_exact == 'google meet'):
          return self.__location[1]
        if(location_exact == 'teams'):
          return self.__location[2]
        if(location_exact == 'jitsi'):
          return self.__location[3]
        #in case of an offer for which no location was specified => 0 element
        if(location_exact == 0):
          return 0

    __time = '' 
    __m_time = '' #f(x) = mx + b -> m 
    __b_time = '' #f(x) = mx + b -> b
    def __init__(self,time={}):
        self.__time = time
    def set_time_utilites(self,time):
        self.__time = time
    def get_time(self):
        return self.__time
    def get_time_utilities(self, time):
        self.__m_time = (self.__time[3] - self.__time[1]) / (self.__time[2] - self.__time[0])
        self.__b_time = self.__time[1] - self.__m_time * self.__time[0] 
        return abs(self.__m_time*time + self.__b_time)

    __issueWeights = '' 
    def __init__(self,issueWeights={}):
        self.__issueWeights = issueWeights
    def set_issueWeights(self,issueWeights):
        self.__issueWeights = issueWeights
    def get_issueWeights(self):
        return self.__issueWeights
    def get_issueWeights_issues(self,issue):
        if(issue == 'day'):
          return self.__issueWeights[0]
        if(issue == 'time'):
          return self.__issueWeights[1]
        if(issue == 'location'):
          return self.__issueWeights[2]
        if(issue == 'duration'):
          return self.__issueWeights[3]
#------------------------------#

#------------------------------#
#Profile 1
profile1_days = [0.5, 0.4, 0.3, 0.2, 0.1] #Monday .. Friday
profile1_location = [0.6, 0.8, 0.1, 0.4] #zoom, google, teams, jitsi
profile1_time = [9, 0.2, 18, 1.0] #lowValue, lowUtility, highValue, highUtility
profile1_duration = [10, 0.2, 120, 0.4] #lowValue, lowUtility, highValue, highUtility
profile1_issueWeights = [0.1, 0.8, 0.05, 0.05] #day, time, location, duration

profile1 = Profile()
profile1.set_day_utilities(profile1_days)
profile1.set_location_utilites(profile1_location)
profile1.set_time_utilites(profile1_time)
profile1.set_duration_utilites(profile1_duration)
profile1.set_issueWeights(profile1_issueWeights)
print("\nRelevant Data Profile 1:")
print("day_utilities: " + str(profile1.get_day_utilities()))
print("location_utilities: " + str(profile1.get_location_utilities()))
print("time_utilities: " + str(profile1.get_time()))
print("duration_utilities: " + str(profile1.get_duration_utilities()))
print("issueWeights: " + str(profile1.get_issueWeights()) + "\n")
#------------------------------#

#------------------------------#
#Profile 2
profile2_days = [0.5, 0.2, 0.3, 0.8, 0.2] #Monday .. Friday
profile2_location = [0.2, 0.1, 0.1, 0.1] #zoom, google, teams, jitsi
profile2_time = [9, 0.8, 18, 1.0] #lowValue, lowUtility, highValue, highUtility
profile2_duration = [10, 0.2, 120, 0.4] #lowValue, lowUtility, highValue, highUtility
profile2_issueWeights = [0.2, 0.4, 0.2, 0.2] #day, time, location, duration

profile2 = Profile()
profile2.set_day_utilities(profile2_days)
profile2.set_location_utilites(profile2_location)
profile2.set_time_utilites(profile2_time)
profile2.set_duration_utilites(profile2_duration)
profile2.set_issueWeights(profile2_issueWeights)
print("\nRelevant Data Profile 2:")
print("day_utilities: " + str(profile2.get_day_utilities()))
print("location_utilities: " + str(profile2.get_location_utilities()))
print("time_utilities: " + str(profile2.get_time()))
print("duration_utilities: " + str(profile2.get_duration_utilities()))
print("issueWeights: " + str(profile2.get_issueWeights()) + "\n")
#------------------------------#

#------------------------------#
#Profile 3
profile3_days = [0.5, 0.1, 0.3, 0.7, 0.1] #Monday .. Friday
profile3_location = [0.6, 0.8, 0.1, 0.4] #zoom, google, teams, jitsi
profile3_time = [9, 0.9, 18, 1.0] #lowValue, lowUtility, highValue, highUtility
profile3_duration = [10, 0.2, 120, 0.4] #lowValue, lowUtility, highValue, highUtility
profile3_issueWeights = [0.1, 0.2, 0.2, 0.5] #day, time, location, duration

profile3 = Profile()
profile3.set_day_utilities(profile3_days)
profile3.set_location_utilites(profile3_location)
profile3.set_time_utilites(profile3_time)
profile3.set_duration_utilites(profile3_duration)
profile3.set_issueWeights(profile3_issueWeights)
print("\nRelevant Data Profile 3:")
print("day_utilities: " + str(profile3.get_day_utilities()))
print("location_utilities: " + str(profile3.get_location_utilities()))
print("time_utilities: " + str(profile3.get_time()))
print("duration_utilities: " + str(profile3.get_duration_utilities()))
print("issueWeights: " + str(profile3.get_issueWeights()) + "\n")
#------------------------------#

#------------------------------#
#copy & paste JSON LOG file -> replace with your path
with open(path_to_JSON_file) as f:
  data = json.load(f)

# load the json data
json_file = data

# Search 'actions' -> in actions the offers (bids) are being stored
if 'actions' in json_file['MOPACState']:
 actions_array = json_file['MOPACState']['actions']

#array for every action
actions_json_array = []

#array for every offer
offers_json_array = []

#populate actions_json_array & offers_json_array
for x in actions_array:
  actions_json_array.append(x)
  offers_json_array.append(json.dumps(x))

#JSON key value we're interested in finding
keyVal = 'offer'

#list that contains all the profiles avalaible in the negotation
profiles = [] #in this case there will be only 3 profiles

# for x in offers_json_array:
#   if keyVal in x:
#     print(x)
#------------------------------#

#------------------------------#
counter = 0
#find profiles (for example: "party65_172_16_2_208") and decide which is 1st, 2md, and 3rd in order to be able to map their respective issues & weights 
for x in offers_json_array:
  if keyVal in x:
    counter = counter + 1

    #find the name of the profiles
    if(counter == 1): 
      elems = re.findall(r'"(.*?)"', x)
      print(re.findall(r'"(.*?)"', x))
      profiles.append(elems[2])
    if(counter == 2):
      print()
      elems = re.findall(r'"(.*?)"', x)
      profiles.append(elems[2])
    if(counter == 3):
      print()
      elems = re.findall(r'"(.*?)"', x)
      profiles.append(elems[2])
    if(counter >= 3):
      break

#order the names of the profiles: for example partyXXXXX1 < partyXXXXX2 < partyXXXXX3
profiles.sort()

#bids for profiles
bids_profile1 = []
bids_profile2 = []
bids_profile3 = []

#specific bids made by Profile 1
bids_profile1_duration = []
bids_profile1_location = []
bids_profile1_time = []
bids_profile1_day = []

#specific bids made by Profile 2
bids_profile2_duration = []
bids_profile2_location = []
bids_profile2_time = []
bids_profile2_day = []

#specific bids made by Profile 3
bids_profile3_duration = []
bids_profile3_location = []
bids_profile3_time = []
bids_profile3_day = []

#overall utilities for every round for each profile
bid_utilities_profile1 = []
bid_utilities_profile2 = []
bid_utilities_profile3 = []
#------------------------------#

#------------------------------#
#add every bid that each profile made
for x in offers_json_array:
  if keyVal in x:
    #an offer looks like this: {"offer": {"actor": "party67_172_16_2_208", "bid": {"issuevalues": {"duration": 60, "location": "google meet", "day": "Monday"}}}}
    print(x)
    #add element to profile 1
    elems = re.findall(r'"(.*?)"', x) #with this we can tell if the current offer is made by Profile 1 || 2 || 3
    if(elems[2] == profiles[0]): #elems[2] represents the profile name -> we check if it's profile 1
      elems = re.findall(r'"bid": {(.*?)}', x)
      elems[0] = elems[0].replace("{", "")

      #find durations bids of profile 1
      elems = re.findall(r'"duration": (.*?),', x)
      if not elems:
        elems = re.findall(r'"duration": (.*?)}', x)
        if not elems:
          bids_profile1_duration.append(0)
        else:
          bids_profile1_duration.append(elems)
      else:
        bids_profile1_duration.append(elems)

      #find location bids of profile 1
      elems = re.findall(r'"location": "(.*?)"', x)
      if not elems:
        bids_profile1_location.append(0)
      else:
        bids_profile1_location.append(elems)
      
      #find time bids of profile 1
      elems = re.findall(r'"time": (.*?),', x)
      if not elems:
        elems = re.findall(r'"time": (.*?)}', x)
        if not elems:
          bids_profile1_time.append(0)
        else: 
          bids_profile1_time.append(elems)
      else:
        bids_profile1_time.append(elems)

      #find day bids of profile 1
      elems = re.findall(r'"day": "(.*?)"', x)
      if not elems:
        bids_profile1_day.append(0)
      else:
        bids_profile1_day.append(elems)

    elif (elems[2] == profiles[1]): 
      elems = re.findall(r'"bid": {(.*?)}', x)
      elems[0] = elems[0].replace("{", "")

      #find durations bids of profile 2
      elems = re.findall(r'"duration": (.*?),', x)
      if not elems:
        elems = re.findall(r'"duration": (.*?)}', x)
        if not elems:
          bids_profile2_duration.append(0)
        else:
          bids_profile2_duration.append(elems)
      else:
        bids_profile2_duration.append(elems)

      #find location bids of profile 2
      elems = re.findall(r'"location": "(.*?)"', x)
      if not elems:
        bids_profile2_location.append(0)
      else:
        bids_profile2_location.append(elems)
      
      #find time bids of profile 2
      elems = re.findall(r'"time": (.*?),', x)
      if not elems:
        elems = re.findall(r'"time": (.*?)}', x)
        if not elems:
          bids_profile2_time.append(0)
        else: 
          bids_profile2_time.append(elems)
      else:
        bids_profile2_time.append(elems)

      #find day bids of profile 2
      elems = re.findall(r'"day": "(.*?)"', x)
      if not elems:
        bids_profile2_day.append(0)
      else:
        bids_profile2_day.append(elems)

    else: 
      elems = re.findall(r'"bid": {(.*?)}', x)
      elems[0] = elems[0].replace("{", "")
      
      #find durations bids of profile 3
      elems = re.findall(r'"duration": (.*?),', x)
      if not elems:
        elems = re.findall(r'"duration": (.*?)}', x)
        if not elems:
          bids_profile3_duration.append(0)
        else:
          bids_profile3_duration.append(elems)
      else:
        bids_profile3_duration.append(elems)

      #find location bids of profile 3
      elems = re.findall(r'"location": "(.*?)"', x)
      if not elems:
        bids_profile3_location.append(0)
      else:
        bids_profile3_location.append(elems)
      
      #find time bids of profile 3
      elems = re.findall(r'"time": (.*?),', x)
      if not elems:
        elems = re.findall(r'"time": (.*?)}', x)
        if not elems:
          bids_profile3_time.append(0)
        else: 
          bids_profile3_time.append(elems)
      else:
        bids_profile3_time.append(elems)

      #find day bids of profile 3
      elems = re.findall(r'"day": "(.*?)"', x)
      if not elems:
        bids_profile3_day.append(0)
      else:
        bids_profile3_day.append(elems)
#------------------------------#


#------------------------------#
#Up to this point we have: Every offer (bid) made by each profile & weights -> Now we compute the utilities for each profile, for each round

count = 0
print("Utilities for profile 1:")
for x in bids_profile1_day:

  #check if for the current bid there exists a day option (yes -> list; no -> 0)
  if(type(bids_profile1_day[count]) == list):
    day = str(bids_profile1_day[count]).replace("['", "")
    day = day.replace("']", "")
  else:
    day = 0
  
  #check if for the current bid there exists a location option (yes -> list; no -> 0)
  if(type(bids_profile1_location[count]) == list):
    location = str(bids_profile1_location[count]).replace("['", "")
    location = location.replace("']", "")
  else:
    location = 0

  #check if for the current bid there exists a time option (yes -> list; no -> 0)
  if(type(bids_profile1_time[count]) == list):
    time = str(bids_profile1_time[count]).replace("['", "")
    time = time.replace("']", "")
  else:
    time = 0

  #check if for the current bid there exists a duration option (yes -> list; no -> 0)
  if(type(bids_profile1_duration[count]) == list):
    duration = str(bids_profile1_duration[count]).replace("['", "")
    duration = duration.replace("']", "")
  else:
    duration = 0  
  #add utility for each round in order to plot later
  bid_utilities_profile1.append(profile1.get_issueWeights_issues('day') * profile1.get_days_utilities(day) + 
                                                                              profile1.get_issueWeights_issues('location') * profile1.get_locations_utilities(location) + 
                                                                              profile1.get_issueWeights_issues('time') * profile1.get_time_utilities(float(time))+ 
                                                                              profile1.get_issueWeights_issues('duration') * profile1.get_durations_utilities(float(duration)))
  
  count += 1
#------------------------------#


#------------------------------#
count = 0
print("Utilities for profile 2:")
for x in bids_profile2_day:

  #check if for the current bid there exists a day option (yes -> list; no -> 0)
  if(type(bids_profile2_day[count]) == list):
    day = str(bids_profile2_day[count]).replace("['", "")
    day = day.replace("']", "")
  else:
    day = 0
  
  #check if for the current bid there exists a location option (yes -> list; no -> 0)
  if(type(bids_profile2_location[count]) == list):
    location = str(bids_profile2_location[count]).replace("['", "")
    location = location.replace("']", "")
  else:
    location = 0

  #check if for the current bid there exists a time option (yes -> list; no -> 0)
  if(type(bids_profile2_time[count]) == list):
    time = str(bids_profile2_time[count]).replace("['", "")
    time = time.replace("']", "")
  else:
    time = 0

  #check if for the current bid there exists a duration option (yes -> list; no -> 0)
  if(type(bids_profile2_duration[count]) == list):
    duration = str(bids_profile2_duration[count]).replace("['", "")
    duration = duration.replace("']", "")
  else:
    duration = 0  

  #add utility for each round in order to plot later
  bid_utilities_profile2.append(profile2.get_issueWeights_issues('day') * profile2.get_days_utilities(day) + 
                                                                              profile2.get_issueWeights_issues('location') * profile2.get_locations_utilities(location) + 
                                                                              profile2.get_issueWeights_issues('time') * profile2.get_time_utilities(float(time))+ 
                                                                              profile2.get_issueWeights_issues('duration') * profile2.get_durations_utilities(float(duration)))
  count += 1
#------------------------------#


#------------------------------#
count = 0
print("Utilities for profile 3:")
for x in bids_profile3_day:

  #check if for the current bid there exists a day option (yes -> list; no -> 0)
  if(type(bids_profile3_day[count]) == list):
    day = str(bids_profile3_day[count]).replace("['", "")
    day = day.replace("']", "")
  else:
    day = 0
  
  #check if for the current bid there exists a location option (yes -> list; no -> 0)
  if(type(bids_profile3_location[count]) == list):
    location = str(bids_profile3_location[count]).replace("['", "")
    location = location.replace("']", "")
  else:
    location = 0

  #check if for the current bid there exists a time option (yes -> list; no -> 0)
  if(type(bids_profile3_time[count]) == list):
    time = str(bids_profile3_time[count]).replace("['", "")
    time = time.replace("']", "")
  else:
    time = 0

  #check if for the current bid there exists a duration option (yes -> list; no -> 0)
  if(type(bids_profile3_duration[count]) == list):
    duration = str(bids_profile3_duration[count]).replace("['", "")
    duration = duration.replace("']", "")
  else:
    duration = 0  
#   print("3_Round: " + str(count) + " utility: (only for day + location) = " + str(profile3.get_issueWeights_issues('day') * profile3.get_days_utilities(day) + 
#                                                                               profile3.get_issueWeights_issues('location') * profile3.get_locations_utilities(location) + 
#                                                                               profile3.get_issueWeights_issues('time') * profile3.get_time_utilities(float(time))+ 
#                                                                               profile3.get_issueWeights_issues('duration') * profile3.get_durations_utilities(float(duration))))

  #add utility for each round in order to plot later
  bid_utilities_profile3.append(profile3.get_issueWeights_issues('day') * profile3.get_days_utilities(day) + 
                                                                              profile3.get_issueWeights_issues('location') * profile3.get_locations_utilities(location) + 
                                                                              profile3.get_issueWeights_issues('time') * profile3.get_time_utilities(float(time))+ 
                                                                              profile3.get_issueWeights_issues('duration') * profile3.get_durations_utilities(float(duration)))
  count += 1
#------------------------------#


#------------------------------#
numOfRounds = []

count = 1
for x in bid_utilities_profile3:
    numOfRounds.append(count)
    count += 1

plt.plot(numOfRounds, bid_utilities_profile1, linestyle='-', solid_joinstyle='miter')
plt.plot(numOfRounds, bid_utilities_profile2, linestyle='-', solid_joinstyle='miter')
plt.plot(numOfRounds, bid_utilities_profile3, linestyle='-', solid_joinstyle='miter')
plt.axis([1, 10, 0, 1])
plt.xticks(numOfRounds)
plt.xlabel("Round")
plt.ylabel("Utility")
plt.grid()
plt.show()

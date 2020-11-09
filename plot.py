import matplotlib.pyplot as plt
import json
import re

#change this to your path to JSON log file from GeniusWeb
path_to_JSON_Session_Results = '/Users/stefanpetrescu/DELFT/AITechniques/Plot_Graphs/file.json'

#change this to your path to JSON meeting profiles -> create a directory that has the json profiles tested on GeniusWeb
path_to_profiles_folder = '/Users/stefanpetrescu/DELFT/AITechniques/Plot_Graphs/profileFiles'


#------------------------------#
#class for groupMeetingProfiles
class Profile:

    profileName_Public = ''
    def __init__(self,profileName={}):  # constructor
        self.profileName_Public = profileName
    def set_profile_name(self,profileName):
        self.profileName_Public = profileName
    def get_profile_name(self):
        return self.profileName_Public

    #day issue
    __day = '' 
    def __init__(self,day={}):  # constructor
        self.__day = day
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
    
    __utilitiesArray = []
    def __init__(self, utilitiesArray={}):
        self.__utilitiesArray = utilitiesArray
    def append_utilitiesArray(self, utilitiesArray):
        if not self.__utilitiesArray:
            self.__utilitiesArray = utilitiesArray
        else:
            self.__utilitiesArray = [self.__utilitiesArray, utilitiesArray]
    def get_utilitiesArray(self):
        return self.__utilitiesArray
    def get_utilitiesArrayPlot(self):
        stringUtilitiesArray = str(self.__utilitiesArray)
        specialCharacters = "[]"
        for char in specialCharacters:
          stringUtilitiesArray = stringUtilitiesArray.replace(char, "")
        
        specialCharacters = " "
        for char in specialCharacters:
          stringUtilitiesArray = stringUtilitiesArray.replace(char, "")

        utilities = stringUtilitiesArray.split(",")
        for x in utilities:
          x = float(x)
        return utilities
#------------------------------#






jsonFiles = []

import os
for file in os.listdir(path_to_profiles_folder):
    if file.endswith(".json"):
        print(os.path.join(path_to_profiles_folder, file))
        jsonFiles.append(os.path.join(path_to_profiles_folder, file))

#all json information for profiles analyzed
profileFilesData = []
jsonProfileFiles = []

profilesObjects = []

count = 0
for jsonProfileFile in jsonFiles:
  with open(jsonFiles[count]) as f:
    profileFilesData.append(json.load(f))
    jsonProfileFiles.append(profileFilesData[count])
    count += 1
#/Users/stefanpetrescu/DELFT/AITechniques/Plot_Graphs/profileFiles


count = 0
for jsonProfileFile in jsonProfileFiles:

  profile = Profile()
  profile.set_profile_name(jsonProfileFiles[count]['LinearAdditiveUtilitySpace']['name'])
  print(jsonProfileFiles[count]['LinearAdditiveUtilitySpace']['name'])

  #compute day issues
  dayString = str(jsonProfileFiles[count]['LinearAdditiveUtilitySpace']['issueUtilities']['day']['discreteutils']['valueUtilities'])
  monday = re.search('Monday(.+?),', dayString)
  tuesday = re.search('Tuesday(.+?),', dayString)
  wednesday = re.search('Wednesday(.+?),', dayString)
  thursday = re.search('Thursday(.+?),', dayString)
  friday = re.search('Fri(.+?)}', dayString)

  mondayUtil = str(monday.group(1))[3:len(monday.group(1))]
  tuesdayUtil = str(tuesday.group(1))[3:len(tuesday.group(1))]
  wednesdayUtil = str(wednesday.group(1))[3:len(wednesday.group(1))]
  thursdayUtil = str(thursday.group(1))[3:len(thursday.group(1))]
  fridayUtil = str(friday.group(1))[6:len(friday.group(1))]

  #print(dayUtilities)
  profile.set_day_utilities([float(mondayUtil), float(tuesdayUtil), float(wednesdayUtil), float(thursdayUtil), float(fridayUtil)])

  #compute time issues
  timeString = str(jsonProfileFiles[count]['LinearAdditiveUtilitySpace']['issueUtilities']['time']['numberutils'])
  lowValue = re.search('lowValue(.+?),', timeString)
  lowUtility = re.search('lowUtility(.+?),', timeString)
  highValue = re.search('highValue(.+?),', timeString)
  highUtility = re.search('highUtility(.+?)}', timeString)

  lowValueUtil = str(lowValue.group(1))[3:len(lowValue.group(1))]
  lowUtilityUtil = str(lowUtility.group(1))[3:len(lowUtility.group(1))]
  highValueUtil = str(highValue.group(1))[3:len(highValue.group(1))]
  highUtilityUtil = str(highUtility.group(1))[3:len(highUtility.group(1))]
  profile.set_time_utilites([float(lowValueUtil), float(lowUtilityUtil), float(highValueUtil), float(highUtilityUtil)])

  #compute location issues
  locationString = str(jsonProfileFiles[count]['LinearAdditiveUtilitySpace']['issueUtilities']['location']['discreteutils']['valueUtilities'])
  zoom = re.search('zoom(.+?),', locationString)
  google = re.search('meet(.+?),', locationString)
  teams = re.search('teams(.+?),', locationString)
  jitsi = re.search('jitsi(.+?)}', locationString)

  zoomUtil = str(zoom.group(1))[3:len(zoom.group(1))]
  googleUtil = str(google.group(1))[3:len(google.group(1))]
  teamsUtil = str(teams.group(1))[3:len(teams.group(1))]
  jitsiUtil = str(jitsi.group(1))[3:len(jitsi.group(1))]
  profile.set_location_utilites([float(zoomUtil), float(googleUtil), float(teamsUtil), float(jitsiUtil)])

  #compute duration issues
  durationString = str(jsonProfileFiles[count]['LinearAdditiveUtilitySpace']['issueUtilities']['duration']['numberutils'])
  lowValue = re.search('lowValue(.+?),', durationString)
  lowUtility = re.search('lowUtility(.+?),', durationString)
  highValue = re.search('highValue(.+?),', durationString)
  highUtility = re.search('highUtility(.+?)}', durationString)

  lowValueUtil = str(lowValue.group(1))[3:len(lowValue.group(1))]
  lowUtilityUtil = str(lowUtility.group(1))[3:len(lowUtility.group(1))]
  highValueUtil = str(highValue.group(1))[3:len(highValue.group(1))]
  highUtilityUtil = str(highUtility.group(1))[3:len(highUtility.group(1))]
  profile.set_duration_utilites([float(lowValueUtil), float(lowUtilityUtil), float(highValueUtil), float(highUtilityUtil)])

  #compute issues weights
  issuesString = str(jsonProfileFiles[count]['LinearAdditiveUtilitySpace']['issueWeights'])
  day = re.search('day(.+?),', issuesString)
  time = re.search('time(.+?),', issuesString)
  location = re.search('location(.+?),', issuesString)
  duration = re.search('duration(.+?)}', issuesString)

  dayUtil = str(day.group(1))[3:len(day.group(1))]
  timeUtil = str(time.group(1))[3:len(time.group(1))]
  locationUtil = str(location.group(1))[3:len(location.group(1))]
  durationUtil = str(duration.group(1))[3:len(duration.group(1))]
  profile.set_issueWeights([float(dayUtil), float(timeUtil), float(locationUtil), float(durationUtil)])

  #add curent JSON file to list of all objects profiles
  profilesObjects.append(profile)

  count += 1 


count = 0
for jsonProfileFile in jsonProfileFiles:
  #print(str(profilesObjects[count].get_profile_name()) + " day: " + str(profilesObjects[count].get_day_utilities()))
  count += 1

profilesObjects = sorted(profilesObjects, key=lambda x: x.profileName_Public)




#copy & paste JSON LOG file -> replace with your path
with open(path_to_JSON_Session_Results) as f:
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

profiles_during_negotiation = []

#add every bid that each profile made
for x in offers_json_array:
  if keyVal in x:
    print(x)
    currentProfile = re.findall(r'"(.*?)"', x)
    #print(re.findall(r'"(.*?)"', x))
    if currentProfile[2] in profiles_during_negotiation:
      continue
    else:
      profiles_during_negotiation.append(currentProfile[2])

profiles_during_negotiation.sort()

count = 0
for x in profiles_during_negotiation:
  print("\n\n\n")
  print("Calculate round utilities for profile: " + str(profilesObjects[count].get_profile_name()) + " aliased by server as: " + str(x))
  #print(str(profilesObjects[count].get_profile_name()) + " day: " + str(profilesObjects[count].get_day_utilities()))
  for offer in offers_json_array:
    if keyVal in offer:
      currentProfile = re.findall(r'"(.*?)"', offer)
      if currentProfile[2] == str(x):
        print(offer)
        # duration location time day

        #offer duration
        offer_duration = 0
        elems = re.findall(r'"duration": (.*?),', offer)
        if not elems:
          elems = re.findall(r'"duration": (.*?)}', offer)
          if not elems:
            offer_duration = 0
          else:
            offer_duration = elems
        else:
          offer_duration = elems
        #print(offer_duration)

        #offer time
        offer_time = 0
        elems = re.findall(r'"time": (.*?),', offer)
        if not elems:
          elems = re.findall(r'"time": (.*?)}', offer)
          if not elems:
            offer_time = 0
          else:
            offer_time = elems
        else:
          offer_time = elems
        #print(offer_time)

        #offer location
        offer_location = 0
        elems = re.findall(r'"location": "(.*?)"', offer)
        if not elems:
          offer_location = 0
        else:
          offer_location = elems
        #print(offer_location)


        #offer day
        offer_day = 0
        elems = re.findall(r'"day": "(.*?)"', offer)
        if not elems:
          offer_day = 0
        else:
          offer_day = elems
        #print(offer_day)
        #print(profilesObjects[count].get_days_utilities(offer_day))
        


        #check if for the current bid there exists a day option (yes -> list; no -> 0)
        if(type(offer_day) == list):
          day = str(offer_day).replace("['", "")
          day = day.replace("']", "")
          #print(day)
        else:
          day = 0
        
        #check if for the current bid there exists a location option (yes -> list; no -> 0)
        if(type(offer_location) == list):
          location = str(offer_location).replace("['", "")
          location = location.replace("']", "")
        else:
          location = 0

        #check if for the current bid there exists a time option (yes -> list; no -> 0)
        if(type(offer_time) == list):
          time = str(offer_time).replace("['", "")
          time = time.replace("']", "")
        else:
          time = 0

        #check if for the current bid there exists a duration option (yes -> list; no -> 0)
        if(type(offer_duration) == list):
          duration = str(offer_duration).replace("['", "")
          duration = duration.replace("']", "")
        else:
          duration = 0  

        bid = profilesObjects[count].get_days_utilities(day) * profilesObjects[count].get_issueWeights_issues('day') \
            + profilesObjects[count].get_locations_utilities(location) * profilesObjects[count].get_issueWeights_issues('location') \
            + profilesObjects[count].get_time_utilities(float(time)) * profilesObjects[count].get_issueWeights_issues('time') \
            + profilesObjects[count].get_durations_utilities(float(duration)) * profilesObjects[count].get_issueWeights_issues('duration')
        print("Current bid for: day = " + str(bid) + " for profile: " + str(profilesObjects[count].get_profile_name()))
        profilesObjects[count].append_utilitiesArray(bid)
        print(profilesObjects[count].get_utilitiesArray())
  count += 1

numOfOffers = 0
for offer in offers_json_array:
    if keyVal in offer:
      numOfOffers += 1

numOfRounds = numOfOffers/len(profilesObjects)

rounds = []

for x in range(int(numOfRounds)):
  rounds.append(x+1)

count = 0
for x in profilesObjects:
  print("Object: " + str(x.get_profile_name()) + "has utility vector = " + str(x.get_utilitiesArray()))
  print("NEW Object: " + str(x.get_profile_name()) + "has utility vector = " + str(x.get_utilitiesArrayPlot()))
  print(len(profilesObjects))
  print(rounds)
  count += 1

utilityProduct = 1
utilityProductToPlot = []

for utility in range(int(numOfRounds)):
    utilityProductToPlot.append(1)

count = 0
for x in profilesObjects:
  list_of_floats = []
  for item in x.get_utilitiesArrayPlot():
    list_of_floats.append(float(item))
  for utility in range(len(list_of_floats)):
    utilityProductToPlot[utility] *= list_of_floats[utility]
  plt.plot(rounds, list_of_floats, linestyle='-', solid_joinstyle='miter', label=x.get_profile_name())
  count += numOfRounds

#plot product utilities
plt.plot(rounds, utilityProductToPlot, linestyle='-', solid_joinstyle='miter', label="Utility Product")

#plot nash product
nashProduct = utilityProductToPlot
maxValueNash = max(utilityProductToPlot)
for utility in range(int(numOfRounds)):
    nashProduct[utility] = maxValueNash
plt.plot(rounds, nashProduct, linestyle='-', solid_joinstyle='miter', label="Nash Product")

#print(utilityProductToPlot)



plt.xticks(rounds)
plt.xlabel("Round")
plt.ylabel("Utility")
plt.axis([1, len(rounds), 0, 1])
plt.grid()
plt.legend(loc='upper center', bbox_to_anchor=(0.5, 1.05), ncol=3, fancybox=True, shadow=True)
plt.show()

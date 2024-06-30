# TODO
## Core
- [X] Calendar view (simple month and dates)
- [x] Add/edit/delete event with dialogue boxes
- [x] Read from .ics file and update view on startup
- [ ] Write to .ics file upon any modification


## Other
- [ ] **Navigation**
	- [ ] Search for events & Go to date
	- [ ] Toggle (show/hide) separate calendars
	- [ ] Separate views (day/month/week/month+day/year/simple event list)
- [ ] **Usability**
	- [ ] Run in background and read .ics every x minutes
	- [ ] Handle multiple calendars stored in separate .ics files
	- [ ] Notification x hours before event
	- [ ] Home screen widget
	- [ ] Customization options (colors, start week on, 24-hour clock, highlight weekends)

## Current
- [x] Read from ICS file
	- [x] Select ICS file
	- [x] Read/write ICS file
	- [x] Save directory to internal app state
	- [x] ICS parser:
		- [x] ICS string -> VEvent object array
		- [x] VEvent object array -> ICS string
	- [x] Convert VEvent object array -> hash map of renderable arrays
- [x] Callback states to parent (Calendar) component
	- [x] current displayed year/month
	- [x] current selected year/month/day
	- [x] events (read from ICS on startup)
- [ ] New activity "+" button
	- [x] When clicked, launch NewEvent activity with start year/month/day as current selected year/month/day
	- [x] If "cancel" clicked, exit the activity
	- [ ] if "add" clicked, add to ICS file and exit the activity
- [ ] Minor improvements
	- [ ] Only do month lookup when month is changed
	- [ ] Add coloring to days with events in monthDays component
	- [ ] Formatting improvement to Event component (prevent text overflowing)
- [x] Create a VEvent lookup class that is initialized from an array of VEvent objects:
	- [x] Implement log n complexity lookup that takes "YYYYMMDD" and returns all events that should be displayed on that day. Consider:
		- [x] One-time events single/multi-day all-day
		- [x] One-time events single/multi-day custom start/end times
		- [x] Repeating events (every x days/weeks/months/years) forever OR till a given date OR after x occurences
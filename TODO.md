# TODO
## Core
- [X] Calendar view (simple month and dates)
- [x] Add/edit/delete event with dialogue boxes
- [ ] Read from .ics file and update view on startup
- [ ] Run in background and read .ics every x minutes
- [ ] Write to .ics file upon any modification
- [ ] Handle multiple calendars stored in separate .ics files

## Other
- [ ] Notification x hours before event
- [ ] Home screen widget
- [ ] Go to date
- [ ] Toggle (show/hide) separate calendars
- [ ] Separate views (day/month/week/month+day/year/simple event list)
- [ ] Customization options (colors, start week on, 24-hour clock, highlight weekends)

## Current
- [ ] Lift up states to parent (MainActivity) component
	- [ ] current displayed year/month
	- [ ] current selected year/month/day
	- [ ] events (read from ICS on startup)
- [ ] New activity "+" button
	- [ ] When clicked, launch NewEvent activity with start year/month/day as current selected year/month/day
	- [ ] If "cancel" clicked, exit the activity
	- [ ] if "add" clicked, add to ICS file, exit the activity, and load from ICS
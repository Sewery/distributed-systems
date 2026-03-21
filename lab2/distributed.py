from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field

app = FastAPI(title="Doodle API")

polls = {}
next_poll_id = 1

class PollCreate(BaseModel):
    title: str = Field(min_length=1)
    options: list[str] = Field(min_length=2)


class PollUpdate(BaseModel):
    title: str = Field(min_length=1)
    options: list[str] = Field(min_length=2)


class VoteIn(BaseModel):
    user: str = Field(min_length=1)
    option: str = Field(min_length=1)

@app.get("/")
def root():
    return {"message": "Doodle API", "docs": "/docs"}

@app.post("/polls")
def create_poll(body: PollCreate):
    global next_poll_id
    poll_id = next_poll_id
    next_poll_id += 1

    polls[poll_id] = {
        "id": poll_id,
        "title": body.title,
        "options": body.options,
        "votes": {}
    }
    return polls[poll_id]

@app.get("/polls")
def list_polls():
    return list(polls.values())

@app.get("/polls/{poll_id}")
def get_poll(poll_id: int):
    poll = polls.get(poll_id)
    if not poll:
        raise HTTPException(status_code=404, detail="Poll not found")
    return poll

@app.put("/polls/{poll_id}")
def update_poll(poll_id: int, body: PollUpdate):
    poll = polls.get(poll_id)
    if not poll:
        raise HTTPException(status_code=404, detail="Poll not found")

    poll["title"] = body.title
    poll["options"] = body.options

    poll["votes"] = {u: o for u, o in poll["votes"].items() if o in body.options}
    return poll

@app.delete("/polls/{poll_id}")
def delete_poll(poll_id: int):
    if poll_id not in polls:
        raise HTTPException(status_code=404, detail="Poll not found")
    del polls[poll_id]
    return {"message": "Poll deleted"}

@app.post("/polls/{poll_id}/votes")
def add_vote(poll_id: int, body: VoteIn):
    poll = polls.get(poll_id)
    if not poll:
        raise HTTPException(status_code=404, detail="Poll not found")
    if body.option not in poll["options"]:
        raise HTTPException(status_code=400, detail="Option not in poll")

    poll["votes"][body.user] = body.option
    return {"message": "Vote saved", "user": body.user, "option": body.option}

@app.put("/polls/{poll_id}/votes/{user}")
def update_vote(poll_id: int, user: str, option: str):
    poll = polls.get(poll_id)
    if not poll:
        raise HTTPException(status_code=404, detail="Poll not found")
    if user not in poll["votes"]:
        raise HTTPException(status_code=404, detail="Vote not found")
    if option not in poll["options"]:
        raise HTTPException(status_code=400, detail="Option not in poll")

    poll["votes"][user] = option
    return {"message": "Vote updated", "user": user, "option": option}


@app.delete("/polls/{poll_id}/votes/{user}")
def delete_vote(poll_id: int, user: str):
    poll = polls.get(poll_id)
    if not poll:
        raise HTTPException(status_code=404, detail="Poll not found")
    if user not in poll["votes"]:
        raise HTTPException(status_code=404, detail="Vote not found")

    del poll["votes"][user]
    return {"message": "Vote deleted"}


@app.get("/polls/{poll_id}/results")
def results(poll_id: int):
    poll = polls.get(poll_id)
    if not poll:
        raise HTTPException(status_code=404, detail="Poll not found")

    counts = {option: 0 for option in poll["options"]}
    for option in poll["votes"].values():
        counts[option] += 1

    return {
        "poll_id": poll_id,
        "title": poll["title"],
        "total_votes": len(poll["votes"]),
        "results": counts
    }
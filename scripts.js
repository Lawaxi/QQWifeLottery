function searchByQQ() {
  const qqInput = document.getElementById('qqInput')
    .value;

  if (!qqInput || isNaN(parseInt(qqInput))) {
    alert('请输入有效的QQ号（整数）');
    return;
  }

  const getIndexData = {
    "qid": parseInt(qqInput)
  };

  // AJAX request for /getIndex
  fetch('/getIndex', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(getIndexData),
    })
    .then(response => response.json())
    .then(indexArray => {
      if (indexArray.length === 0) {
        alert('无抽奖记录');
        return;
      }

      const resultContainer = document.getElementById('resultContainer');
      resultContainer.innerHTML = '';

      indexArray.forEach(index => {
        const accountElement = document.createElement('div');
        accountElement.className = 'account-element';

        const idP = document.createElement('p');
        idP.id = "id";
        idP.textContent = index;
        const groupP = document.createElement('p');
        groupP.id = "group";
        const qidP = document.createElement('p');
        qidP.id = "qid";
        accountElement.appendChild(idP);
        accountElement.appendChild(groupP);
        accountElement.appendChild(qidP);
        
        const recordsDiv = document.createElement('div');
        const totalRecordsP = document.createElement('p');
        totalRecordsP.className = "count";
        recordsDiv.appendChild(totalRecordsP);
        const topFiveRecords = document.createElement('div');
        topFiveRecords.id = "topRecords";
        topFiveRecords.className = 'flex-container';
        recordsDiv.appendChild(topFiveRecords);
        const otherRecords = document.createElement('div');
        otherRecords.id = "otherRecords";
        otherRecords.className = 'flex-container can-fold';
        otherRecords.style.display = "none";
        recordsDiv.appendChild(otherRecords);
        const expandLink = document.createElement('a');
        expandLink.href = '#';
        expandLink.textContent = '展开所有';
        expandLink.addEventListener('click', expand);
        recordsDiv.appendChild(expandLink);
        accountElement.appendChild(recordsDiv);

        const wivesDiv = document.createElement('div');
        const totalWivesP = document.createElement('p');
        totalWivesP.className = "count";
        wivesDiv.appendChild(totalWivesP);
        const topWivesContainer = document.createElement('div');
        topWivesContainer.id = "topWivesContainer";
        topWivesContainer.className = 'flex-container';
        wivesDiv.appendChild(topWivesContainer);
        const otherWivesContainer = document.createElement('div');
        otherWivesContainer.id = "otherWivesContainer";
        otherWivesContainer.className = 'flex-container can-fold';
        otherWivesContainer.style.display = "none";
        wivesDiv.appendChild(otherWivesContainer);
        const expandLink2 = document.createElement('a');
        expandLink2.href = '#';
        expandLink2.textContent = '展开所有';
        expandLink2.addEventListener('click', expand);
        wivesDiv.appendChild(expandLink2);
        accountElement.appendChild(wivesDiv);


        // AJAX request for /getUserLoc
        fetch('/getUserLoc', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({
              "index": index
            }),
          })
          .then(response => response.json())
          .then(userData => {
            groupP.textContent = userData.group;

            qidP.textContent = userData.qid;
          });

        // AJAX request for /getWives
        fetch('/getWives', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({
              "index": index
            }),
          })
          .then(response => response.json())
          .then(wivesData => {
            const total = Object.values(wivesData)
              .reduce((acc, value) => acc + value, 0);
            totalRecordsP.textContent = `共带走 ${total} 人`;

            const sortedKeys = Object.keys(wivesData)
              .sort((a, b) => wivesData[b] - wivesData[a]);
            const topFiveKeys = sortedKeys.slice(0, 5);
            const otherKeys = sortedKeys.slice(5);

            topFiveKeys.forEach((key, i) => {
              const flexP = document.createElement('p');
              flexP.className = 'flex-item';
              flexP.textContent = `${key}(${wivesData[key]})`;
              topFiveRecords.appendChild(flexP);
            });

            otherKeys.forEach(key => {
              const flexP = document.createElement('p');
              flexP.className = 'flex-item';
              flexP.textContent = `${key}(${wivesData[key]})`;
              otherRecords.appendChild(flexP);
            });
          });

        // AJAX request for /getWivesMaxSense
        fetch('/getWivesMaxSense', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({
              "index": index
            }),
          })
          .then(response => response.json())
          .then(maxSenseData => {
            totalWivesP.textContent = `您是 ${maxSenseData.length} 位成员的情愫王`;
            maxSenseData.sort((a, b) => b.sense - a.sense);

            // Fetch additional information from /getAllStarData
            fetch('/getAllStarData')
              .then(response => response.json())
              .then(allStarData => {
                maxSenseData.forEach((wifeData, i) => {
                  const matchingMember = allStarData.find(member => member.sid == wifeData.sid);

                    const wifeElement = document.createElement('div');
                    wifeElement.className = 'wife-element';

                    const image = document.createElement('img');
                    image.className = "member-photo"
                    image.src = `https://www.snh48.com/images/member/zp_${wifeData.sid}.jpg`;
                    image.onerror = function() {
                      this.src = 'https://www.snh48.com/images/member/zp_def.jpg';
                    };
                    wifeElement.appendChild(image);
                    
                    const memberInfo = document.createElement('div');
                    memberInfo.className = "member-info"
                  if (matchingMember) {
                    const nameP = document.createElement('p');
                    nameP.id = "name";
                    nameP.textContent = matchingMember.s;
                    memberInfo.appendChild(nameP);
                    
                    const senseP = document.createElement('p');
                    senseP.id = "sense";
                    senseP.textContent = wifeData.sense;
                    memberInfo.appendChild(senseP);

                    const teamP = document.createElement('p');
                    teamP.id = "team";
                    teamP.textContent = `${matchingMember.g} ${matchingMember.t}`;
                    memberInfo.appendChild(teamP);

                    const periodP = document.createElement('p');
                    periodP.id = "period";
                    periodP.textContent = matchingMember.p;
                    memberInfo.appendChild(periodP);

                    const birthdayP = document.createElement('p');
                    birthdayP.id = "birthday";
                    birthdayP.textContent = matchingMember.birthday;
                    memberInfo.appendChild(birthdayP);
                  }else {
                    const nameP = document.createElement('p');
                    nameP.id = "name";
                    nameP.textContent = "未知成员";
                    memberInfo.appendChild(nameP);
                    
                    const senseP = document.createElement('p');
                    senseP.id = "sense";
                    senseP.textContent = wifeData.sense;
                    memberInfo.appendChild(senseP);

                    const teamP = document.createElement('p');
                    nameP.id = "team";
                    teamP.textContent = `sid: ${wifeData.sid}`;
                    memberInfo.appendChild(teamP);
                  }
                  
                  wifeElement.appendChild(memberInfo);
                  
                    if (i < 5) {
                      topWivesContainer.appendChild(wifeElement);
                    } else {
                      otherWivesContainer.appendChild(wifeElement);
                    }
                });
              });
          });
      resultContainer.appendChild(accountElement);
      });

    });
}

function expand(event) {
  const divs = event.target.parentElement.querySelectorAll('.can-fold');
  divs.forEach(div => div.style.display = div.style.display === 'none' ? 'flex' : 'none');
  event.target.textContent = event.target.textContent === '展开所有' ? '收起' : '展开所有';
}
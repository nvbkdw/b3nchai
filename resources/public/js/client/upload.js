const Uppy = require('@uppy/core')
const Dashboard = require('@uppy/dashboard')
const AwsS3 = require('@uppy/aws-s3')

var uppy = Uppy()
var csrf = document.getElementById("__anti-forgery-token").value
uppy.use(Dashboard, {
  inline: true,
  target: '#drag-drop-area'
})

uppy.use(AwsS3, {
  getUploadParameters (file) {
    return fetch('/s3-sign', {
      method: 'post',
      headers: {
        accept: 'application/json',
        'content-type': 'application/json',
        'X-CSRF-Token': csrf,
      },
      body: JSON.stringify({
        filename: file.name,
        contentType: file.type
      })
    }).then((response) => {
      return response.json()
    }).then((data) => {
      return {
        method: data.method,
        url: data.url,
        fields: data.fields,
        headers: data.headers
      }
    })
  }
})

uppy.on('upload-success', (file, data) => {
  return fetch('/benchmark', {
    method: 'post',
    headers: {
      accept: 'application/json',
      'content-type': 'application/json',
      'X-CSRF-Token': csrf,
    },
    body: JSON.stringify({
      filename: file.name
    })
  }).then((response) => {
    console.log(response)
  }).catch(error =>
    console.log("Error: " + error)
  )
})
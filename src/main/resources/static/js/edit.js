'use strict';
let saveData = {};
/********************************************************************************************************************************************
Common Func
********************************************************************************************************************************************/
function readURL($input,replaceStr,selector) {
	let reader = new FileReader();
	try{
		reader.onload = function(e) {
	      	$(`${selector}`).attr('src', e.target.result);	
	    };
	    reader.readAsDataURL($input.files[0]);
	    saveData[`${$input.id}`] = $input.files[0]; 		
	}catch(err){
		console.error(err);
		return false;
	}
	return true;
}

function selectorClick(elNum,selector){
	$(selector+elNum).trigger( 'click' );
}


function save(){
	let _templateCode = getTemplateCode();
	
	if(!validCheck(_templateCode)){
		return false;
	}
	 
	
	let formData = new FormData();
		formData.append("templateCode", getTemplateCode());
		for(let key in saveData){
		  formData.append(key, saveData[key]);
		}
		
	if(_templateCode === "2"){
		formData.append("qrcodeUrl", $("#qrcodeUrl").val().trim());
		formData.append("qrCodeLocation",$("#qrCodeLocation option:selected").val());
		formData.append("qrcodeBgColor",$("#qrcodeBgColor").val());
		formData.append("qrcodeColor",$("#qrcodeColor").val());
		formData.append("bgTransparent",`${$("#bgTransparent").prop("checked") ? "Y" : "N"}`);
	}
	
	const progressBar = document.getElementById('progressPst');
	
	$.ajax({
	    type : 'post',          
	    url : '/api/v1/stream/save',
	    async : true,
	    enctype:'multipart/form-data',
	    contentType : false,
	    processData : false,
	    dataType:'json',
	    data : formData,
	    xhr: function(){
			const xhr = $.ajaxSettings.xhr();
			xhr.upload.onprogress = function(e){
				var per = e.loaded * 100 / e.total;
				if(per < 95){
					progressBar.value = `${per}`;
					console.log(per);	
				}
			};
			return xhr;
		},
		success : function(result) { // 결과 성공 콜백함수
	        console.info(result);
	        progressBar.value = 100;
	        if(result["status"] === "success"){
				$("#resultVideo").attr('src', `/api/v1/stream/${result['resultFileName']}`);
				alert("동영상 제작 완료");
			}
	    },
	    complete: function () {
			//성공, 실패와 상관없이 실행하고 싶은 로직
			progressBar.value = 0;
		},
	    error : function(request, status, error) { // 결과 에러 콜백함수
	        console.log(error);
	        alert("동영상 제작 실패 \n 관리자에게 문의하세요.");
	    }
	})
	
	return true;
}

function validCheck(tCode){
	if(tCode === null || tCode === undefined){
		alert("템플릿 코드가 유효하지 않습니다.");
		return false;
	}
	
	if(tCode === "2"){
		if($("#qrcodeUrl").val().trim() === ""){
			alert("qrCdoe URL 을 입력해주세요.");
			$("#qrcodeUrl").focus();
			return false;
		}
	}
	
	if($.isEmptyObject(saveData) ){
		alert("저장할 데이터가 없습니다.");
		return false;
	}
	
	return true;
}

function getTemplateCode(){
	let _templateCode = location.pathname.replace("/pages/templateEdit/","");
	return _templateCode; 
}

function editForm(_flag){
	if(_flag){
		
	}else{
		
	}
}



/********************************************************************************************************************************************
Editor Func
********************************************************************************************************************************************/

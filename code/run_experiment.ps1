<#
Simple PowerShell helper to compile and run Experiment sweeps.
Usage (from the repo `code` folder):
  ./run_experiment.ps1 -Mode sweep -Sample 100 -File data/000000.txt -Start 1 -End 8 -Step 1 -Out sweep.csv
  ./run_experiment.ps1 -Mode dist  -File data/000000.txt -High 6 -Out dist.csv
#>

param(
    [string]$Mode = "sweep",
    [int]$Sample = 100,
    [string]$File = "data/000000.txt",
    [int]$Start = 1,
    [int]$End = 8,
    [int]$Step = 1,
    [int]$High = 6,
    [string]$Out = "experiment_out.csv"
)

Push-Location $PSScriptRoot
Write-Host "Compiling Java sources..."
javac -d out *.java
if ($LASTEXITCODE -ne 0) {
    Write-Error "Compilation failed"
    Pop-Location
    exit 1
}

if ($Mode -eq "sweep") {
    $cmd = "java -cp out Experiment sweep $Sample $File $Start $End $Step $Out"
}
else {
    $cmd = "java -cp out Experiment dist $File $High $Out"
}

Write-Host "Running: $cmd"
iex $cmd

Write-Host "Done. Output: $PSScriptRoot\$Out"
Pop-Location
